package space.siy.waveformview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor

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
 * A WaveFormView show WaveFormData given
 *
 * You have to build [WaveFormData] first using [WaveFormData.Factory]
 *
 * Note: Do not use View.INVISIBLE visibility flag, use View.GONE instead. Otherwise some weired
 * UI bug may happen.
 */
class FixedWaveFormView(
  context: Context,
  attr: AttributeSet?,
  defStyleAttr: Int
) : View(context, attr, defStyleAttr) {
  constructor(context: Context) : this(context, null, 0)
  constructor(
    context: Context,
    attr: AttributeSet
  ) : this(context, attr, 0)

  private var barShader =
    LinearGradient(0f, 0f, 0f, 700f, Color.RED, Color.GRAY, Shader.TileMode.CLAMP)
  private val blockPaint: Paint

  /**
   * Width each block
   */
   private val blockWidth: Float

  /**
   * Radius of top dome
   */
  private val domeRadius: Float

  /**
   * Flag to enable/disable top dome drawing
   */
  private val domeDrawEnabled: Boolean

  /**
   * Flag to enable/disable seek on touch/drag
   */
  private val seekEnabled: Boolean

  /**
   * Gap needs to be between two consecutive bar
   */
  private val gapWidth: Float

  /**
   * Scale of top blocks
   */
  private val topBlockScale: Float

  /**
   * Scale of bottom blocks
   */
  private val bottomBlockScale: Float

  /**
   * Color used in played blocks
   */
  private val blockColorPlayed: Int

  /**
   * Color used in blocks default
   */
  private val blockColor: Int

  private val verticalGap: Float

  init {
    val lp =
      context.obtainStyledAttributes(attr, R.styleable.FixedWaveFormView, defStyleAttr, 0)
    blockWidth = lp.getDimension(R.styleable.FixedWaveFormView_blockWidth, 5f)
    gapWidth = lp.getDimension(R.styleable.FixedWaveFormView_gapWidth, 2f)
    verticalGap = lp.getDimension(R.styleable.FixedWaveFormView_verticalGap, 0f)
    domeDrawEnabled = lp.getBoolean(R.styleable.FixedWaveFormView_domeDrawEnabled, false)
    seekEnabled = lp.getBoolean(R.styleable.FixedWaveFormView_seekEnabled, false)
    topBlockScale = lp.getFloat(R.styleable.FixedWaveFormView_topBlockScale, 1f)
    bottomBlockScale = lp.getFloat(R.styleable.FixedWaveFormView_bottomBlockScale, 0f)
    blockColor = lp.getColor(R.styleable.FixedWaveFormView_blockColor, Color.WHITE)
    blockColorPlayed = lp.getColor(R.styleable.FixedWaveFormView_blockColorPlayed, Color.RED)
    blockPaint = Paint()
    domeRadius = blockWidth / 2
    lp.recycle()
  }

  private var upperWaveBars: Array<RectF>? = null
  private var bottomWaveBars: Array<RectF>? = null

  // api
  var duration: Long = DEFAULT_DURATION

  /**
   * WaveFormData show in view
   */
  var waveFormData: WaveFormData? = null
    set(value) {
      field = value

      duration = DEFAULT_DURATION
      if (value != null && value.duration > DEFAULT_DURATION) {
        duration = value.duration
      }

      if (value == null) return
      doOnLayout {
        CoroutineScope(Dispatchers.Default).launch {
          val possibleBlockCountOnScreen =
              floor((width + (2 * gapWidth)) / (blockWidth + gapWidth)).toInt()
          resampleData = FloatArray(possibleBlockCountOnScreen)
          if (value.samples.size > possibleBlockCountOnScreen) {
            val numberOfDataToNormalize: Int = value.samples.size / possibleBlockCountOnScreen
            for (i in 0 until possibleBlockCountOnScreen) {
              resampleData[i] = value.samples.average(
                  i * numberOfDataToNormalize, (i + 1) * numberOfDataToNormalize
              )
            }
          }

          withContext(Dispatchers.Main) {
            requestDraw()
          }
        }
      }
    }

  var data: FloatArray = FloatArray(0)
    set(value) {
      if (value.isEmpty()) throw IllegalArgumentException("data must not be empty")
      if (duration == DEFAULT_DURATION) throw IllegalStateException("duration must be set before")
      field = value
      resampleData = data
      requestDraw()
    }

  /**
   * position in milliseconds
   */
  var position: Long = 0
    set(value) {
      if (seeking) return
      val lastValue = field
      field = value
      if (position == 0L) {
        lastDeltaProgress = 0
        seekingPosition = 0
      } else if (position == duration) {
        seekingPosition = position
      }
      if (position - lastValue >= 0 && position >= seekingPosition) {
        lastDeltaProgress = position - lastValue
        seekingPosition = position
      } else {
        seekingPosition += lastDeltaProgress
      }
      invalidate()
    }

  private var lastDeltaProgress = 0L


  /**
   * @see Callback
   */
  var callback: Callback? = null

  /**
   * The resampled data to show
   *
   * This generate when [waveFormData] set
   */
  var resampleData = FloatArray(0)
    private set

  private var offsetX = 0f
  private var seekingPosition = 0L

  @SuppressLint("DrawAllocation")
  override fun onDraw(canvas: Canvas) {
    offsetX = (width / duration.toFloat()) * seekingPosition
    // Right now, I don't have any better way than allocating shader in every invalidate()
    // invocation
    barShader = LinearGradient(
        offsetX, 0f, offsetX + 1, 0f, blockColorPlayed, blockColor, Shader.TileMode.CLAMP
    )
    blockPaint.shader = barShader

    // Draw data points
    if (upperWaveBars?.size == 0 && bottomWaveBars?.size == 0) {
      return
    }

    val dataSize = resampleData.size
    upperWaveBars?.forEachIndexed { i, rect ->
      if (i < dataSize) {
        canvas.drawRect(rect, blockPaint)
      } else {
        canvas.drawArc(rect, 180f, 180f, true, blockPaint)
      }
    }
    bottomWaveBars?.forEachIndexed { i, rect ->
      if (i < dataSize) {
        canvas.drawRect(rect, blockPaint)
      } else {
        canvas.drawArc(rect, 0f, 180f, true, blockPaint)
      }
    }
  }

  private var lastTapTime = 0L
  private var paused = false
  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (!seekEnabled) {
      return false
    }

    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        lastTapTime = System.currentTimeMillis()
      }
      MotionEvent.ACTION_MOVE -> {
        seekingCount++
        if (seekingCount > SEEKING_THRESHOLD) seeking = true
        if (seeking) {
          if (!paused) {
            paused = true
            callback?.onSeekStarted()
          }
          seekingPosition = (duration * event.x.toLong()) / width
        }
      }
      MotionEvent.ACTION_UP -> {
        seekingCount = 0
        paused = false

        if (seeking) {
          seeking = false
          seekingPosition = (duration * event.x.toLong()) / width
          callback?.onSeek(seekingPosition)
        } else {
          if (System.currentTimeMillis() - lastTapTime <= TAP_THRESHOLD_TIME) {
            callback?.onTap()
          }
        }
      }
    }
    invalidate()
    return true
  }

  private fun requestDraw() {
    if (ViewCompat.isLaidOut(this)) {
      drawBars()
    } else {
      doOnLayout {
        drawBars()
      }
      requestLayout()
    }
  }

  private fun drawBars() {
    upperWaveBars = generateUpperBars(resampleData)
    bottomWaveBars = generateBottomBars(resampleData)
    invalidate()
  }

  /**
   * Extension method to average data in specific range
   */
  private fun ShortArray.average(
    start: Int,
    end: Int
  ): Float {
    var sum = 0.0
    for (i in start until end)
      sum += Math.abs(this[i].toDouble())

    return sum.toFloat() / (end - start)
  }

  fun forceComplete() {
    position = duration
  }

  private var seeking = false
  /**
   * Count up ACTION_MOVE event
   */
  private var seekingCount = 0

  /**
   * It provide a simple callback to sync your MediaPlayer
   */
  interface Callback {
    /**
     * Called when view tapped
     */
    fun onTap()

    /**
     * Called when gestures detects as an attempt to seek
     */
    fun onSeekStarted()

    /**
     * Called when seek complete
     * @param pos Position in milliseconds
     */
    fun onSeek(pos: Long)
  }

  companion object {
    const val SEEKING_THRESHOLD = 4
    const val TAP_THRESHOLD_TIME = 300L
    const val MIN_HEIGHT = 2
    const val DEFAULT_DURATION = 1L // We use this in some division, so don't make it zero
  }

  // Generate upper bar and dome rects
  private fun generateUpperBars(resampleData: FloatArray): Array<RectF> {
    val maxAmplitude = resampleData.max()!!
    if (topBlockScale <= 0 || resampleData.isEmpty() || !maxAmplitude.isFinite()) {
      return emptyArray()
    }

    val dataSize = resampleData.size
    val upperBars = Array(dataSize) { i ->
      val multiplier = i.toFloat()
      val x = (multiplier * blockWidth) + (multiplier * gapWidth)
      val bottom = height * topBlockScale
      var top = bottom - (bottom * resampleData[i] / maxAmplitude)
      top = if (domeDrawEnabled) (top + domeRadius) else top
      val paddedTop = if (bottom - top < MIN_HEIGHT) (bottom - MIN_HEIGHT) else top
      RectF(x, paddedTop, x + blockWidth, bottom)
    }

    val upperDomes = if (domeDrawEnabled) Array(dataSize) { i ->
      val upperRect = upperBars[i]
      val bottom = upperRect.bottom
      val paddedTop = upperRect.top
      val domeTop =
        if (paddedTop + MIN_HEIGHT == bottom) paddedTop - MIN_HEIGHT else paddedTop - domeRadius
      val domeBottom =
        if (paddedTop + MIN_HEIGHT == bottom) paddedTop + MIN_HEIGHT else paddedTop + domeRadius
      RectF(upperRect.left, domeTop, upperRect.left + blockWidth, domeBottom)
    } else emptyArray()

    return upperBars + upperDomes
  }

  // Generate bottom bar and dome rects
  private fun generateBottomBars(resampleData: FloatArray): Array<RectF> {
    val maxAmplitude = resampleData.max()!!
    if (bottomBlockScale <= 0 || resampleData.isEmpty() || !maxAmplitude.isFinite()) {
      return emptyArray()
    }

    val bottomBars = Array(resampleData.size) { i ->
      val multiplier = i.toFloat()
      val x = (multiplier * blockWidth) + (multiplier * gapWidth)
      val bottom = (height - height * bottomBlockScale) + verticalGap
      var top = bottom + ((height - bottom) * resampleData[i] / maxAmplitude)
      top = if (domeDrawEnabled) (top - domeRadius) else top
      val paddedTop = if (top - bottom < MIN_HEIGHT) (bottom + MIN_HEIGHT) else top
      RectF(x, paddedTop, x + blockWidth, bottom)
    }

    val bottomDomes = if (domeDrawEnabled) Array(resampleData.size) { i ->
      val bottomRect = bottomBars[i]
      val bottom = bottomRect.bottom
      val paddedTop = bottomRect.top
      val domeTop =
        if (paddedTop - MIN_HEIGHT == bottom) paddedTop + MIN_HEIGHT else paddedTop - domeRadius
      val domeBottom =
        if (paddedTop - MIN_HEIGHT == bottom) paddedTop - MIN_HEIGHT else paddedTop + domeRadius
      RectF(bottomRect.left, domeTop, bottomRect.left + blockWidth, domeBottom)
    } else emptyArray()

    return bottomBars + bottomDomes
  }
}
