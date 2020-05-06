package space.siy.waveformview

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaDataSource
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Copyright 2018 siy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *Data object contains raw sample data of 16bit short array and some params
 *
 * You have to use [WaveFormData.Factory] to build
 *
 * @property sampleRate Sample rate of data
 * @property channel Count of channel
 * @property duration Duration of data in milliseconds
 * @property samples Raw sample data
 */
class WaveFormData private constructor(
    val sampleRate: Int,
    val channel: Int,
    val duration: Long,
    var samples: ShortArray = ShortArray(0)
) : Parcelable {
  private constructor(
      sampleRate: Int,
      channel: Int,
      duration: Long,
      stream: ByteArrayOutputStream
  ) : this(sampleRate, channel, duration) {
    samples = stream.toShortArray()
  }

  private fun ByteArrayOutputStream.toShortArray(): ShortArray {
    val array = ByteBuffer.wrap(this.toByteArray())
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
    val results = ShortArray(array.remaining())
    array.get(results)
    return results
  }

  /**
   * Factory class to build [WaveFormData]
   *
   * Note : It build data asynchronously
   */
  class Factory {

    /**
     *Provide callbacks indicating progress to the user.
     */
    interface Callback {
      /**
       * Called when complete
       * Take out your data built
       *@param waveFormData built data
       */
      fun onComplete(waveFormData: WaveFormData)

      /**
       * Called if for any reason getting data from an audio file fails
       */
      fun onFailed(e: Exception)
    }

    private val extractor = MediaExtractor()
    private var audioTrackIndex = -1
    var dataLoadingJob: Job? = null

    private constructor()

    /**
     * Sets the data source (AssetFileDescriptor) to use.
     * It is the caller's responsibility to close the file descriptor.
     * It is safe to do so as soon as this call returns.
     * @param afd the AssetFileDescriptor for the file you want to extract from.
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    @RequiresApi(24)
    constructor(afd: AssetFileDescriptor) {
      extractor.setDataSource(afd)
      init()
    }

    /**
     * Sets the data source as a content Uri.
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to extract from.
     * @param headers the headers to be sent together with the request for the data. This can be
     * null if no specific headers are to be sent with the request.
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    constructor(
        context: Context,
        uri: Uri,
        headers: Map<String, String>
    ) {
      extractor.setDataSource(context, uri, headers)
      init()
    }

    /**
     * Sets the data source (FileDescriptor) to use.
     * It is the caller's responsibility to close the file descriptor.
     * It is safe to do so as soon as this call returns.
     * @param fd the FileDescriptor for the file you want to extract from.
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    constructor(fd: FileDescriptor) {
      extractor.setDataSource(fd)
      init()
    }

    /**
     * Sets the data source (MediaDataSource) to use.
     * @param dataSource the MediaDataSource for the media you want to extract from
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    @RequiresApi(23)
    constructor(dataSource: MediaDataSource) {
      extractor.setDataSource(dataSource)
      init()
    }

    /**
     * Sets the data source (FileDescriptor) to use.
     * The FileDescriptor must be seekable (N.B. a LocalSocket is not seekable).
     * It is the caller's responsibility to close the file descriptor.
     * It is safe to do so as soon as this call returns.
     * @param fd the FileDescriptor for the file you want to extract from.
     * @param offset the offset into the file where the data to be extracted starts, in bytes
     * @param length the length in bytes of the data to be extracted
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    constructor(
        fd: FileDescriptor,
        offset: Long,
        length: Long
    ) {
      extractor.setDataSource(fd, offset, length)
      init()
    }

    /**
     * Sets the data source (file-path or http URL) to use.
     * @param path the path of the file, or the http URL of the stream
     * When path refers to a local file, the file may actually be opened by a process other than
     * the calling application. This implies that the pathname should be an absolute path (as any
     * other process runs with unspecified current working directory),
     * and that the pathname should reference a world-readable file.
     * As an alternative, the application could first open the file for reading,
     * and then use the file descriptor form setDataSource(FileDescriptor).
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    constructor(path: String) {
      extractor.setDataSource(path)
      init()
    }

    /**
     * Sets the data source (file-path or http URL) to use.
     * @param path the path of the file, or the http URL
     * When path refers to a network file the android.Manifest.permission.INTERNET permission is
     * required.
     * @param headers the headers associated with the http request for the stream you want to play.
     * This can be null if no specific headers are to be sent with the request.
     * @throws Exception If media doesn't have audio track, throw exception.
     */
    constructor(
        path: String,
        headers: Map<String, String>
    ) {
      extractor.setDataSource(path, headers)
      init()
    }

    private fun init() {
      if (extractor.trackCount == 0) throw IllegalStateException("No track")
      audioTrackIndex = extractor.getAudioTrackIndex()
      if (audioTrackIndex == -1) throw IllegalStateException("No audio track")
      extractor.selectTrack(audioTrackIndex)
    }

    private fun MediaExtractor.getAudioTrackIndex(): Int {
      for (i in 0 until trackCount) {
        // select audio track
        if (getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.contains("audio/") == true) {
          return i
        }
      }
      return -1
    }

    /**
     * Build a data using constructor params
     *
     * Note : It works asynchronously and takes several seconds
     *
     * @param callback callback to report progress and pass built data
     */
    fun build(callback: Callback) {
      val format = extractor.getTrackFormat(audioTrackIndex)
      val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME))
      codec.configure(format, null, null, 0)
      val outFormat = codec.outputFormat
      codec.start()

      dataLoadingJob = CoroutineScope(Dispatchers.IO).launch {
        var eos = false
        val stream = ByteArrayOutputStream(512)
        val info = MediaCodec.BufferInfo()

        while (!eos) {
          val inputBufferId = codec.dequeueInputBuffer(10)
          if (inputBufferId >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferId)
            if (inputBuffer == null) {
              callback.onFailed(
                  RuntimeException("codec.getInputBuffer(inputBufferId) returned null")
              )
              return@launch
            }

            val readSize = extractor.readSampleData(inputBuffer, 0)
            extractor.advance()
            codec.queueInputBuffer(
                inputBufferId, 0, if (readSize > 0) readSize else 0, extractor.sampleTime,
                if (readSize > 0) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
          }

          val outputBufferId = codec.dequeueOutputBuffer(info, 10)
          if (outputBufferId >= 0) {
            val outputBuffer = codec.getOutputBuffer(outputBufferId)
            if (outputBuffer == null) {
              callback.onFailed(
                  RuntimeException("codec.getOutputBuffer(outputBufferId) returned null")
              )
              return@launch
            }

            val buffer = ByteArray(outputBuffer.remaining())
            outputBuffer.get(buffer)
            stream.write(buffer)
            codec.releaseOutputBuffer(outputBufferId, false)
            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
              eos = true
            }
          }
        }
        codec.stop()
        codec.release()

        try {
          val data = WaveFormData(
              outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
              outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
              extractor.getTrackFormat(audioTrackIndex)
                  .getLong(MediaFormat.KEY_DURATION) / 1000,
              stream
          )
          withContext(Dispatchers.Main) {
            callback.onComplete(data)
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            callback.onFailed(e)
          }
        }
      }
    }

    fun cancel() {
      dataLoadingJob?.cancel()
    }
  }

  override fun writeToParcel(
      parcel: Parcel,
      flags: Int
  ) {
    parcel.writeInt(sampleRate)
    parcel.writeInt(channel)
    parcel.writeLong(duration)
    parcel.writeArray(samples.toTypedArray())
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<WaveFormData> {
    override fun createFromParcel(parcel: Parcel): WaveFormData {
      val sampleRate = parcel.readInt()
      val channel = parcel.readInt()
      val duration = parcel.readLong()
      val samples = parcel.readArray(Array<Short>::class.java.classLoader) as Array<Short>

      return WaveFormData(sampleRate, channel, duration, samples.toShortArray())
    }

    override fun newArray(size: Int): Array<WaveFormData?> {
      return arrayOfNulls(size)
    }
  }
}
