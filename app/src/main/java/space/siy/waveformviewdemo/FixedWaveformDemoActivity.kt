package space.siy.waveformviewdemo

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnPlayPause
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnStop
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.progressBar1
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.progressBar2
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.waveFormView1
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.waveFormView2
import space.siy.waveformview.FixedWaveFormPlayer
import space.siy.waveformview.FixedWaveFormPlayer.Callback
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FixedWaveformDemoActivity : AppCompatActivity() {

  private var waveFormPlayer1: FixedWaveFormPlayer? = null
  private var waveFormPlayer2: FixedWaveFormPlayer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fitted_waveform_demo)

    try {
      val audioPath1 = getRawResourcePath("audio_sample_mp3", "mp3")
      waveFormPlayer1 = FixedWaveFormPlayer(audioPath1!!,
          getSystemService(Context.AUDIO_SERVICE) as AudioManager)
      waveFormPlayer1?.snapToStartAtCompletion = false
      progressBar1.visibility = View.VISIBLE
      waveFormPlayer1?.loadInto(waveFormView1, object : Callback {
        override fun onLoadingComplete() {
          waveFormPlayer1?.play()
          progressBar1.visibility = View.GONE
        }

        override fun onError() {
          progressBar1.visibility = View.GONE
        }

        override fun onPlay() {
        }

        override fun onPause() {
        }

        override fun onStop() {

        }
      })

      val audioPath2 = getRawResourcePath("audio_sample_mp3", "mp3")
      waveFormPlayer2 = FixedWaveFormPlayer(audioPath2!!,
          getSystemService(Context.AUDIO_SERVICE) as AudioManager)
      progressBar2.visibility = View.VISIBLE
      waveFormPlayer2?.loadInto(waveFormView2, object : Callback {
        override fun onLoadingComplete() {
          progressBar2.visibility = View.GONE
        }

        override fun onError() {
          btnPlayPause.text = "play"
          progressBar2.visibility = View.GONE
        }

        override fun onPlay() {
          btnPlayPause.text = "pause"
        }

        override fun onPause() {
          btnPlayPause.text = "play"
        }

        override fun onStop() {
          btnPlayPause.text = "play"
        }
      })
      btnPlayPause.setOnClickListener {
        if (waveFormPlayer2?.isPlaying() == true) {
          waveFormPlayer2?.pause()
        } else {
          waveFormPlayer2?.play()
        }
      }
      btnStop.setOnClickListener {
        btnPlayPause.text = "play"
        waveFormPlayer2?.stop()
      }
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    waveFormPlayer1?.dispose()
    waveFormPlayer2?.dispose()
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
