package space.siy.waveformviewdemo

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import space.siy.waveformview.WaveFormData
import space.siy.waveformview.WaveFormView
import space.siy.waveformviewdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Open From Assets Folder
        val afd = assets.openFd("jazz_in_paris.mp3")

        //Build WaveFormData
        WaveFormData.Factory(afd.fileDescriptor, afd.startOffset, afd.length)
                .build(object : WaveFormData.Factory.Callback {
                    //When Complete, you can receive data and set to the view
                    override fun onComplete(waveFormData: WaveFormData) {
                        binding.progressBar.visibility = View.GONE

                        binding.waveFormView.data = waveFormData


                        //UI setup
                        binding.seekBar1.progress = (binding.waveFormView.secPerBlock*100f).toInt()
                        binding.seekBar2.progress = (binding.waveFormView.blockWidth).toInt()
                        binding.seekBar3.progress = (binding.waveFormView.topBlockScale*100f).toInt()
                        binding.seekBar4.progress = (binding.waveFormView.bottomBlockScale*100f).toInt()

                        binding.seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                binding.waveFormView.secPerBlock = seekBar.progress / 100f
                            }
                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })
                        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                binding.waveFormView.blockWidth = seekBar.progress.toFloat()
                            }
                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })
                        binding.seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                binding.waveFormView.topBlockScale = seekBar.progress / 100f
                            }
                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })
                        binding.seekBar4.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                binding.waveFormView.bottomBlockScale = seekBar.progress / 100f
                            }
                            override fun onStartTrackingTouch(seekBar: SeekBar) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        })

                        //Initialize MediaPlayer
                        val player = MediaPlayer()
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        player.prepare()
                        player.start()

                        //Synchronize with MediaPlayer using WaveFormView.Callback
                        binding.waveFormView.callback = object : WaveFormView.Callback {
                            override fun onPlayPause() {
                                if (player.isPlaying)
                                    player.pause()
                                else
                                    player.start()
                            }
                            override fun onSeek(pos: Long) {
                                player.seekTo(pos.toInt())
                            }
                        }

                        //You have to notify current position to the view
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                binding.waveFormView.position = player.currentPosition.toLong()
                                Handler().postDelayed(this, 20)
                            }
                        }, 20)

                    }

                    override fun onProgress(progress: Float) {
                        binding.progressBar.progress = (progress*10).toInt()
                    }
                })
    }

}
