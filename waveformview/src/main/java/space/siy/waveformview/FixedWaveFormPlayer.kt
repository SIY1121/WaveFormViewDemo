package space.siy.waveformview

import android.media.MediaPlayer
import android.os.Handler

class FixedWaveFormPlayer(val filePath: String) {
  private val waveFormDataFactory: WaveFormData.Factory = WaveFormData.Factory(filePath)
  private val handler = Handler()
  private var waveFormView: FixedWaveFormView? = null
  private var callback: Callback? = null
  private var player: MediaPlayer? = null

  private val runnable = object : Runnable {
    override fun run() {
      val currentPosition = player?.currentPosition?.toLong()
      waveFormView?.position = currentPosition ?: 0
      if (player?.isPlaying == true) {
        handler.postDelayed(this, REFRESH_DELAY_MILLIS)
      }
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
          // Notify complete
          this@FixedWaveFormPlayer.callback?.onLoadingComplete()
        }
        player?.prepareAsync()

        wfv?.callback = object : FixedWaveFormView.Callback {
          override fun onPlay() {
            play()
          }

          override fun onPause() {
            pause()
          }

          override fun onSeek(pos: Long) {
            player?.seekTo(pos.toInt())
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        this@FixedWaveFormPlayer.callback?.onError()
      }
    }

    override fun onProgress(v: Float) {
      this@FixedWaveFormPlayer.callback?.onLoadingProgress(v)
    }
  }

  fun loadInto(waveFormView: FixedWaveFormView, callback: Callback) {
    this.waveFormView = waveFormView
    this.callback = callback
    waveFormDataFactory.build(factoryCallback)
  }

  fun play() {
    if (!isPlaying()) {
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
      player?.pause()
      if (player != null) {
        callback?.onPause()
        handler.removeCallbacks(runnable)
      }
    }
  }

  fun stop() {
    player?.pause()
    player?.seekTo(0)
    waveFormView?.position = 0
    callback?.onStop()
  }

  fun isPlaying(): Boolean = player?.isPlaying == true

  fun dispose() {
    waveFormDataFactory.cancel()
    waveFormView = null
    callback = null
    player?.release()
  }

  interface Callback {
    fun onLoadingProgress(float: Float)
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