package space.siy.waveformview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.system.measureTimeMillis

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

  init {
    val lp =
      context.obtainStyledAttributes(attr, R.styleable.FixedWaveFormView, defStyleAttr, 0)
    blockWidth = lp.getDimension(R.styleable.FixedWaveFormView_blockWidth, 5f)
    gapWidth = lp.getDimension(R.styleable.FixedWaveFormView_gapWidth, 2f)
    domeDrawEnabled = lp.getBoolean(R.styleable.FixedWaveFormView_domeDrawEnabled, false)
    topBlockScale = lp.getFloat(R.styleable.FixedWaveFormView_topBlockScale, 1f)
    bottomBlockScale = lp.getFloat(R.styleable.FixedWaveFormView_bottomBlockScale, 0f)
    blockColor = lp.getColor(R.styleable.FixedWaveFormView_blockColor, Color.WHITE)
    blockColorPlayed = lp.getColor(R.styleable.FixedWaveFormView_blockColorPlayed, Color.RED)
    blockPaint = Paint()
    domeRadius = blockWidth / 2
    lp.recycle()
  }

  /**
   * WaveFormData show in view
   */
  var data: WaveFormData? = null
    set(value) {
      field = value
      if (value == null) return
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
        invalidate()
      }
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
      } else if (position == data?.duration) {
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
   * This generate when [data] set
   */
  private var resampleData = FloatArray(0)

  private var offsetX = 0f
  private var seekingPosition = 0L

  @SuppressLint("DrawAllocation")
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    measureTimeMillis {
      offsetX = (width / (data?.duration ?: 1L).toFloat()) * seekingPosition
      // Right now, I don't have any better way than allocating shader in every invalidate()
      // invocation
      barShader = LinearGradient(
          offsetX, 0f, offsetX + 1, 0f, blockColorPlayed, blockColor, Shader.TileMode.CLAMP
      )
      blockPaint.shader = barShader

      val p = Paint()
      p.color = Color.RED
      p.style = FILL

      // Draw data points
      if (resampleData.isNotEmpty()) {
        val maxAmplitude = resampleData.max()!!
        for (i in 0 until resampleData.size) {
          val multiplier = i.toFloat()
          val x = (multiplier * blockWidth) + (multiplier * gapWidth)
          if (topBlockScale > 0f) {
            val bottom = height * topBlockScale
            var top = bottom - (bottom * resampleData[i] / maxAmplitude)
            top = if (domeDrawEnabled) (top + domeRadius) else top
            val paddedTop = if (bottom - top < 2) (bottom - 2) else top
            canvas.drawRect(x, paddedTop, x + blockWidth, bottom, blockPaint)
            if (domeDrawEnabled) {
              val domeTop = if (paddedTop + 2 == bottom) paddedTop - 2 else paddedTop - domeRadius
              val domeBottom =
                if (paddedTop + 2 == bottom) paddedTop + 2 else paddedTop + domeRadius
              canvas.drawArc(
                  x, domeTop, x + blockWidth, domeBottom, 180f, 180f,
                  true, blockPaint
              )
            }
          }
          if (bottomBlockScale > 0f) {
            val bottom = (height - height * bottomBlockScale) + gapWidth
            val top = bottom + ((height - bottom) * resampleData[i] / maxAmplitude)
            //canvas.drawRect(x, top - domeRadius, x + blockWidth, bottom, blockPaint)
            if (domeDrawEnabled) {
//              canvas.drawArc(
//                  x, top - blockWidth, x + blockWidth, top, 0f, 180f, true, blockPaint
//              )
            }
          }
        }
      }
    }.also {
      Log.d("FixedWaveFormView", "Time took: ${it}ms")
    }

  }

  private var lastTapTime = 0L
  private var paused = false
  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
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
          seekingPosition = ((data?.duration ?: 1L) * event.x.toLong()) / width
        }
      }
      MotionEvent.ACTION_UP -> {
        seekingCount = 0
        paused = false

        if (seeking) {
          seeking = false
          seekingPosition = ((data?.duration ?: 1L) * event.x.toLong()) / width
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
    position = data?.duration ?: 0
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
  }
}
