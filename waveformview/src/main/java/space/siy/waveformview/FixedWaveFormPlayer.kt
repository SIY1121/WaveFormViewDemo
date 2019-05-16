package space.siy.waveformview

import android.content.ContentValues.TAG
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Handler
import android.util.Log

class FixedWaveFormPlayer(
  private val filePath: String,
  private val audioManager: AudioManager
) : OnAudioFocusChangeListener {

  private val waveFormDataFactory: WaveFormData.Factory = WaveFormData.Factory(filePath)
  private val handler = Handler()
  private var waveFormView: FixedWaveFormView? = null
  private var callback: Callback? = null
  private var player: MediaPlayer? = null
  var snapToStartAtCompletion = true

  private val runnable = object : Runnable {
    override fun run() {
      updatePosition()
      if (player?.isPlaying == true) {
        handler.postDelayed(this, REFRESH_DELAY_MILLIS)
      }
    }
  }

  private fun updatePosition() {
    val currentPosition = player?.currentPosition?.toLong()
    waveFormView?.position = currentPosition ?: 0
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
          // Notify complete
          this@FixedWaveFormPlayer.callback?.onLoadingComplete()
        }
        player?.prepareAsync()
        player?.setOnCompletionListener {
          waveFormView?.forceComplete()
          if (snapToStartAtCompletion) {
            stop()
          } else {
            releaseAudioFocus()
          }
          callback?.onStop()
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
      requestAudioFocus()
      player?.start()
      if (player != null) {
        callback?.onPlay()
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, REFRESH_DELAY_MILLIS)
      }
    }
  }

  fun pause() {
    if (isPlaying()) {
      releaseAudioFocus()
      player?.pause()
      if (player != null) {
        callback?.onPause()
        handler.removeCallbacks(runnable)
      }
    }
  }

  fun stop() {
    releaseAudioFocus()
    player?.pause()
    player?.seekTo(0)
    waveFormView?.position = 0
  }

  private fun toggleSpeakerphone(on: Boolean) {
    audioManager.isSpeakerphoneOn = on
  }

  private fun requestAudioFocus() {
    val result = audioManager.requestAudioFocus(
        this, AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN
    )
    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.e(TAG, "AUDIO FOCUS - REQUEST DENIED")
    }
  }

  private fun releaseAudioFocus() {
    val result = audioManager.abandonAudioFocus(this)
    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.e(TAG, "AUDIO FOCUS ABANDON - REQUEST DENIED")
    }
  }

  fun isPlaying(): Boolean = player?.isPlaying == true

  override fun onAudioFocusChange(focusChange: Int) {
  }

  fun dispose() {
    waveFormDataFactory.cancel()
    waveFormView = null
    callback = null
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