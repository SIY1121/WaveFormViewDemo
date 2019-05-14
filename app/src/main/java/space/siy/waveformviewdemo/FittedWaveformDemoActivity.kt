package space.siy.waveformviewdemo

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import space.siy.waveformview.FittedWaveFormView
import space.siy.waveformview.WaveFormData
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FittedWaveformDemoActivity : AppCompatActivity() {

  internal var handler = Handler()
  internal var delayMillis = 20L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fitted_waveform_demo)

    val audioPath = getRawResourcePath("audio_sample_mp3", "mp3")
    try {
      val fd = FileInputStream(audioPath!!).fd
      WaveFormData.Factory(fd)
          .build(
              object : WaveFormData.Factory.Callback {
                override fun onComplete(waveFormData: WaveFormData) {
                  val waveFormView_1 = findViewById<FittedWaveFormView>(R.id.waveView_1)
                  waveFormView_1.data = waveFormData
                  waveFormView_1.position = 0

                  val waveFormView_2 = findViewById<FittedWaveFormView>(R.id.waveView_2)
                  waveFormView_2.data = waveFormData
                  waveFormView_2.position = 0

                  // Initialize MediaPlayer
                  try {
                    val player = MediaPlayer()
                    player.setDataSource(audioPath)
                    player.prepare()

                    val runnable = object : Runnable {
                      override fun run() {
                        val currentPosition = player.currentPosition.toLong()
                        waveFormView_1.position = currentPosition
                        waveFormView_2.position = currentPosition
                        if (player.isPlaying) {
                          handler.postDelayed(this, delayMillis)
                        }
                      }
                    }

                    player.setOnPreparedListener {
                      //You have to notify current position to the view
                      player.start()
                      handler.postDelayed(runnable, delayMillis)
                    }

                    val callback = object : FittedWaveFormView.Callback {
                      override fun onPlay() {
                        if (!player.isPlaying) {
                          player.start()
                          handler.removeCallbacks(runnable)
                          handler.postDelayed(runnable, delayMillis)
                        }
                      }

                      override fun onPause() {
                        if (player.isPlaying) {
                          player.pause()
                          handler.removeCallbacks(runnable)
                        }
                      }

                      override fun onSeek(pos: Long) {
                        player.seekTo(pos.toInt())
                      }
                    }

                    waveFormView_1.callback = callback
                    waveFormView_2.callback = callback

                  } catch (e: IOException) {
                    e.printStackTrace()
                  }

                }

                override fun onProgress(v: Float) {
                  Log.d("MainActivity", "Progress: $v")
                  //progressBar.progress = (progress*10).toInt()
                }
              })
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  private fun getRawResourcePath(
    name: String,
    extension: String
  ): String? {
    var absolutePath: String? = null
    val uri = Uri.parse("android.resource://$packageName/raw/$name")
    try {
      val inputStream = contentResolver.openInputStream(uri)
      val saveFile = File("$filesDir/temp.$extension")
      val outputStream = FileOutputStream(saveFile)
      inputStream?.copyTo(outputStream)
      absolutePath = saveFile.absolutePath
    } catch (e: IOException) {
      e.printStackTrace()
    }

    return absolutePath
  }
}
