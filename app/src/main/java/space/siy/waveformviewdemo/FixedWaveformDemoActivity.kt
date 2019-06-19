package space.siy.waveformviewdemo

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnPlayPause
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnSpeakerToggle
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.btnStop
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.progressBar1
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.progressBar2
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.tvWaveFormView2Duration
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.waveFormView1
import kotlinx.android.synthetic.main.activity_fitted_waveform_demo.waveFormView2
import space.siy.waveformview.FixedWaveFormPlayer
import space.siy.waveformview.FixedWaveFormPlayer.Callback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FixedWaveformDemoActivity : AppCompatActivity() {

  private var waveFormPlayer1: FixedWaveFormPlayer? = null
  private var waveFormPlayer2: FixedWaveFormPlayer? = null

  private val waveFormData1 =
      floatArrayOf(271.18964f, 1868.1785f, 2085.129f, 4660.3794f, 1495.1597f, 723.6413f, 3326.2505f,
          4594.4644f, 1007.93695f, 1491.2094f, 3670.496f, 4129.4453f, 2376.5815f, 1892.7697f,
          8125.0776f, 10066.781f, 7049.2812f, 8684.718f, 8335.306f, 9624.154f, 8531.999f,
          4281.7827f, 3455.2278f, 3236.3586f, 7426.999f, 8297.676f, 5793.3604f, 6201.068f,
          5951.427f, 5908.605f, 2989.6033f, 832.4345f, 185.71243f, 92.81306f)
  private val waveFormDuration1 = 3448

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fitted_waveform_demo)
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager;

    try {
      val audioPath1 = getRawResourcePath("audio_sample_mp3", "mp3")
      waveFormPlayer1 = FixedWaveFormPlayer(audioPath1!!, audioManager)
      waveFormPlayer1?.snapToStartAtCompletion = false
      progressBar1.visibility = View.VISIBLE

      // Shows uses of cached data
      waveFormPlayer1?.loadInto(waveFormView1, waveFormData1, waveFormDuration1, object: Callback {
        override fun onLoadingComplete() {
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

      val audioPath2 = getRawResourcePath("audio_greetings", "wav")
      waveFormPlayer2 = FixedWaveFormPlayer(audioPath2!!, audioManager)
      progressBar2.visibility = View.VISIBLE
      waveFormPlayer2?.loadInto(waveFormView2, object : Callback {
        override fun onLoadingComplete() {
          progressBar2.visibility = View.GONE
          tvWaveFormView2Duration.text = waveFormPlayer2?.duration.toString()
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
      btnSpeakerToggle.setOnClickListener {
        waveFormPlayer2?.toggleSpeakerphone(!audioManager.isSpeakerphoneOn)
      }
    } catch (e: Exception) {
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
