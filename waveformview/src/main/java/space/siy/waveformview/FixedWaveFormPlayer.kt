package space.siy.waveformview

import android.content.ContentValues.TAG
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class FixedWaveFormPlayer(
    private val filePath: String,
    private val audioManager: AudioManager
) : OnAudioFocusChangeListener {

  // public apis
  var snapToStartAtCompletion = true
  /**
   * Duration will only yield correct result after the Callback.onLoadingComplete() is called
   */
  var duration = 0
    private set

  private val waveFormDataFactory: WaveFormData.Factory = WaveFormData.Factory(filePath)
  private var waveFormView: FixedWaveFormView? = null
  private var callback: Callback? = null
  private var player: MediaPlayer? = null
  private var playSuspended = false
  private var focusRequest: AudioFocusRequest? = null
  private var uiUpdateJob: Job? = null

  private fun updatePosition() {
    try {
      val currentPosition = player?.currentPosition?.toLong()
      waveFormView?.position = currentPosition ?: 0
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  private val factoryCallback = object : WaveFormData.Factory.Callback {
    override fun onComplete(waveFormData: WaveFormData) {
      val wfv = this@FixedWaveFormPlayer.waveFormView
      wfv?.data = waveFormData
      wfv?.position = 0

      // Initialize MediaPlayer
      try {
        player = MediaPlayer()
        player?.setDataSource(filePath)
        player?.setOnPreparedListener {
          duration = player?.duration ?: 0
          // Notify complete
          this@FixedWaveFormPlayer.callback?.onLoadingComplete()
        }
        player?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        player?.prepareAsync()
        player?.setOnCompletionListener {
          waveFormView?.forceComplete()
          stop(snapToStartAtCompletion)
        }

        wfv?.callback = object : FixedWaveFormView.Callback {
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
        e.printStackTrace()
        releaseAudioFocus()
        this@FixedWaveFormPlayer.callback?.onError()
      }
    }
  }

  fun loadInto(waveFormView: FixedWaveFormView, callback: Callback) {
    this.waveFormView = waveFormView
    this.callback = callback
    waveFormDataFactory.build(factoryCallback)
  }

  fun play() {
    if (!isPlaying()) {
      if (!playSuspended) {
        requestAudioFocus()
      }
      player?.start()
      if (player != null) {
        callback?.onPlay()
        uiUpdateJob?.cancel()
        uiUpdateJob = CoroutineScope(Dispatchers.Default).launch {
          do {
            updatePosition()
            delay(REFRESH_DELAY_MILLIS)
          } while (player?.isPlaying == true)
        }
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
      player?.seekTo(0)
      waveFormView?.position = 0
    }
    callback?.onStop()
  }

  fun toggleSpeakerphone(on: Boolean) {
    audioManager.isSpeakerphoneOn = on
  }

  private fun requestAudioFocus() {
    val result =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
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
            this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
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

  fun isPlaying(): Boolean = player?.isPlaying == true

  override fun onAudioFocusChange(focusChange: Int) {
    when (focusChange) {
      AudioManager.AUDIOFOCUS_GAIN -> if (playSuspended) play()
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
    waveFormDataFactory.cancel()
    waveFormView = null
    callback = null
    releaseAudioFocus()
    playSuspended = false
    player?.release()
  }

  interface Callback {
    fun onLoadingComplete()
    fun onError()
    fun onPlay()
    fun onPause()
    fun onStop()
  }

  companion object {
    const val REFRESH_DELAY_MILLIS = 20L
  }
}
