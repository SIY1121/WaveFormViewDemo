# WaveFormView

WaveFormView provides a view to display audio waveforms.

Generating waveforms at runtime, you don't have to prepare data in advance.

**Note : It takes a few seconds to generate**

# Screenshots
<img src="https://github.com/SIY1121/WaveFormViewDemo/blob/master/screenshots/sample1.png" width="40%"/><img src="https://github.com/SIY1121/WaveFormViewDemo/blob/master/screenshots/sample2.png" width="40%" />

# Importing the Liblary
Add the following dependicity to your `build.gradle` file.

```
dependencies {
    repositories {
        jcenter()
    }
    compile 'space.siy:waveformview:1.0.0'
}
```

# Usage
You can see full code at [MainActivity.kt](https://github.com/SIY1121/WaveFormViewDemo/blob/master/app/src/main/java/space/siy/waveformviewdemo/MainActivity.kt)

```kotlin
//Open From Assets Folder
val afd = assets.openFd("jazz_in_paris.mp3")

//Build WaveFormData
WaveFormData.Factory(afd.fileDescriptor, afd.startOffset, afd.length)
        .build(object : WaveFormData.Factory.Callback {
            //When Complete, you can receive data and set to the view
            override fun onComplete(waveFormData: WaveFormData) {
                waveFormView.data = waveFormData

                //Initialize MediaPlayer
                val player = MediaPlayer()
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                player.prepare()
                player.start()

                //Synchronize with MediaPlayer using WaveFormView.Callback
                waveFormView.callback = object : WaveFormView.Callback {
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
                        waveFormView.position = player.currentPosition.toLong()
                        Handler().postDelayed(this, 20)
                    }
                }, 20)

            }

            override fun onProgress(progress: Float) {
                progressBar.progress = (progress*10).toInt()
            }
        })
```

# Customization
You can change block style via xml and program.

The following xml shows all attributes.


```xml
<space.siy.waveformview.WaveFormView
        android:id="@+id/waveFormView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:blockColor="@color/white"
        app:blockColorPlayed="@color/red"
        app:showTimeText="true"
        app:textColor="@color/white"
        app:textBgColor="@color/black"
        app:blockWidth="10"
        app:topBlockScale="1"
        app:bottomBlockScale="0.5"
        app:peakMode="peakmode_average"
        app:secPerBlock="0.1" />
```
# Document

See [here](https://github.com/SIY1121/WaveFormViewDemo/blob/master/waveformview/build/markdown/space.siy.waveformview/index.md).

# Requirement
Supports Android 5.0+

WaveFormView uses MediaCodec supporting only 5.0 or higher to generate waveform at runtime.

# Lisence

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this work except in compliance with the License.
> You may obtain a copy of the License in the LICENSE file, or at:
>
>  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
