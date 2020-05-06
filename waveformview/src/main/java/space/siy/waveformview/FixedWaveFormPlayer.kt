package space.siy.waveformview

import android.content.ContentValues.TAG
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class FixedWaveFormPlayer(
  private val filePath: String,
  context: Context
) : OnAudioFocusChangeListener {

  companion object {
    const val REFRESH_DELAY_MILLIS = 20L
  }

  // public apis
  var snapToStartAtCompletion = true
  /**
   * Duration will only yield correct result after the Callback.onLoadingComplete() is called
   */
  var duration = 0
    private set

  private var waveFormDataFactory: WaveFormData.Factory? = null
  private var waveFormView: FixedWaveFormView? = null
  private var callback: Callback? = null
  private var player: MediaPlayer? = null
  private var playSuspended = false
  private val audioManager: AudioManager =
    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private var focusRequest: AudioFocusRequest? = null
  private var uiUpdateJob: Job? = null
  private var dataLoadingJob: Job? = null

  private fun updatePosition() {
    val currentPosition = player?.currentPosition?.toLong()
    waveFormView?.position = currentPosition ?: 0
  }

  private val factoryCallback = object : WaveFormData.Factory.Callback {
    override fun onComplete(waveFormData: WaveFormData) {
      val wfv = this@FixedWaveFormPlayer.waveFormView
      wfv?.waveFormData = waveFormData
      wfv?.position = 0

      initMediaPlayer()
    }

    override fun onFailed(e: Exception) {
      callback?.onError(e)
    }
  }

  private fun initMediaPlayer() {
    // Initialize MediaPlayer
    try {
      player = MediaPlayer()
      player?.setDataSource(filePath)
      player?.setOnPreparedListener {
        duration = player?.duration ?: 0
        // Notify complete
        callback?.onLoadingComplete()
      }
      player?.setAudioAttributes(
          AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
      )
      player?.prepareAsync()
      player?.setOnCompletionListener {
        waveFormView?.forceComplete()
        stop(snapToStartAtCompletion)
      }

      waveFormView?.callback = object : FixedWaveFormView.Callback {
        override fun onTap() {
          if (player?.isPlaying == true) {
            pause()
          } else {
            play()
          }
        }

        override fun onSeekStarted() {
          pause()
        }

        override fun onSeek(pos: Long) {
          player?.seekTo(pos.toInt())
        }
      }
    } catch (e: Exception) {
      releaseAudioFocus()
      callback?.onError(e)
    }
  }

  fun loadInto(waveFormView: FixedWaveFormView, callback: Callback) {
    loadInto(waveFormView, FloatArray(0), 1, callback)
  }

  fun loadInto(
    waveFormView: FixedWaveFormView,
    data: FloatArray,
    duration: Int,
    callback: Callback
  ) {
    this.waveFormView = waveFormView
    this.callback = callback
    if (data.isEmpty()) {
      dataLoadingJob?.cancel()
      dataLoadingJob = CoroutineScope(Dispatchers.Main).launch {
        waveFormDataFactory = withContext(Dispatchers.IO) { WaveFormData.Factory(filePath) }
        if (dataLoadingJob?.isActive == true) {
          waveFormDataFactory?.build(factoryCallback)
        }
      }
    } else {
      waveFormView.duration = duration.toLong()
      waveFormView.data = data
      waveFormView.position = 0
      initMediaPlayer()
    }
  }

  fun play() {
    if (!isPlaying()) {
      if (!playSuspended) {
        requestAudioFocus()
      }
      try {
        player?.start()
        if (player != null) {
          callback?.onPlay()
          uiUpdateJob?.cancel()
          uiUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            try {
              do {
                updatePosition()
                delay(REFRESH_DELAY_MILLIS)
              } while (player?.isPlaying == true)
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }
        }
      } catch (ignored: Exception) {
        stop()
      }
    }
  }

  fun pause() {
    if (isPlaying()) {
      player?.pause()
      if (player != null) {
        callback?.onPause()
        uiUpdateJob?.cancel()
      }
    }
  }

  fun stop() {
    stop(true)
  }

  private fun stop(snapToStart: Boolean) {
    playSuspended = false
    releaseAudioFocus()
    if (isPlaying()) {
      player?.pause()
    }
    if (snapToStart) {
      try {
        player?.seekTo(0)
      } catch (ignored: Exception) { }
      waveFormView?.position = 0
    }
    callback?.onStop()
    uiUpdateJob?.cancel()
  }

  // toggle depends on audio mode. So caller needs to make sure proper audio mode is
  // set, otherwise the request is a no-op.
  fun toggleSpeakerphone(on: Boolean) {
    audioManager.isSpeakerphoneOn = on
  }

  private fun requestAudioFocus() {
    val result =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        focusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(
                    AudioAttributes.CONTENT_TYPE_MUSIC
                ).build()
            )
            .setOnAudioFocusChangeListener(this)
            .build()
        audioManager.requestAudioFocus(focusRequest!!)
      } else {
        audioManager.requestAudioFocus(
            this, AudioManager.STREAM_MUSIC, AUDIOFOCUS_GAIN_TRANSIENT
        )
      }
    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.e(TAG, "AUDIO FOCUS - REQUEST DENIED")
    }
  }

  private fun releaseAudioFocus() {
    val result =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (focusRequest != null) {
          audioManager.abandonAudioFocusRequest(focusRequest!!)
        } else {
          AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }
      } else {
        audioManager.abandonAudioFocus(this)
      }
    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.e(TAG, "AUDIO FOCUS ABANDON - REQUEST DENIED")
    }
  }

  fun isPlaying(): Boolean = try {
    player?.isPlaying == true
  } catch (e: Exception) {
    false
  }

  override fun onAudioFocusChange(focusChange: Int) {
    when (focusChange) {
      AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
        if (isPlaying()) {
          playSuspended = true
          pause()
        }
      }
      AudioManager.AUDIOFOCUS_LOSS -> stop()
    }
  }

  fun dispose() {
    dataLoadingJob?.cancel()
    waveFormDataFactory?.cancel()
    waveFormView = null
    callback = null
    uiUpdateJob?.cancel()
    uiUpdateJob = null
    releaseAudioFocus()
    playSuspended = false
    player?.release()
  }

  /**
   * Api to get resampled data drawn by FixedWaveFormView.
   * Note: It doesn't guarantee when the data will be available, but if no data is available
   * in any instance, it'll return a zero sized array.
   */
  fun resampledData(): FloatArray = waveFormView?.resampleData ?: FloatArray(0)

  interface Callback {
    fun onLoadingComplete()
    fun onError(e: Exception)
    fun onPlay()
    fun onPause()
    fun onStop()
  }
}
