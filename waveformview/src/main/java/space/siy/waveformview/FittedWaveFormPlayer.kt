package space.siy.waveformview

import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.view.View
import java.io.FileDescriptor
import java.io.IOException
import java.lang.ref.WeakReference

class FittedWaveFormPlayer(val filePath: String) {
  private val waveFormDataFactory: WaveFormData.Factory = WaveFormData.Factory(filePath)
  private val handler = Handler()
  private var waveFormView: FittedWaveFormView? = null
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

  fun loadInto(waveFormView: FittedWaveFormView, callback: Callback) {
    this.waveFormView = waveFormView
    this.callback = callback

    waveFormDataFactory.build(
        object : WaveFormData.Factory.Callback {
          override fun onComplete(waveFormData: WaveFormData) {
            val wfv = this@FittedWaveFormPlayer.waveFormView
            wfv?.data = waveFormData
            wfv?.position = 0

            // Initialize MediaPlayer
            try {
              player = MediaPlayer()
              player?.setDataSource(filePath)
              player?.setOnPreparedListener {
                // Notify complete
                this@FittedWaveFormPlayer.callback?.onComplete()
              }
              player?.prepare()

              val fittedWaveFormViewCallback = object : FittedWaveFormView.Callback {
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

              wfv?.callback = fittedWaveFormViewCallback
            } catch (e: Exception) {
              e.printStackTrace()
              this@FittedWaveFormPlayer.callback?.onError()
            }
          }

          override fun onProgress(v: Float) {
            this@FittedWaveFormPlayer.callback?.onProgress(v)
          }
        })
  }

  fun play() {
    if (player?.isPlaying == false) {
      player?.start()
      if (player != null) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, REFRESH_DELAY_MILLIS)
      }
    }
  }

  fun pause() {
    if (player?.isPlaying == true) {
      player?.pause()
      if (player != null) {
        handler.removeCallbacks(runnable)
      }
    }
  }

  fun stop() {
    player?.seekTo(0)
  }

  fun dispose() {
    waveFormDataFactory.cancel()
    waveFormView = null
    callback = null
    player?.release()
  }

  interface Callback {
    fun onProgress(float: Float)
    fun onComplete()
    fun onError()
  }

  companion object {
    const val REFRESH_DELAY_MILLIS = 20L
  }
}