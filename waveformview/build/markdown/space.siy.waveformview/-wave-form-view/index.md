[space.siy.waveformview](../index.md) / [WaveFormView](./index.md)

# WaveFormView

`class WaveFormView : View`

A WaveFormView show WaveFormData given

You have to build [WaveFormData](../-wave-form-data/index.md) first using [WaveFormData.Factory](../-wave-form-data/-factory/index.md)

### Types

| Name | Summary |
|---|---|
| [Callback](-callback/index.md) | `interface Callback`<br>It provide a simple callback to sync your MediaPlayer |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `WaveFormView(context: Context)`<br>`WaveFormView(context: Context, attr: AttributeSet)``WaveFormView(context: Context, attr: AttributeSet?, defStyleAttr: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)`<br>A WaveFormView show WaveFormData given |

### Properties

| Name | Summary |
|---|---|
| [blockColor](block-color.md) | `var blockColor: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Color used in blocks default |
| [blockColorPlayed](block-color-played.md) | `var blockColorPlayed: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Color used in played blocks |
| [blockWidth](block-width.md) | `var blockWidth: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Width each block |
| [bottomBlockScale](bottom-block-scale.md) | `var bottomBlockScale: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Scale of bottom blocks |
| [callback](callback.md) | `var callback: `[`Callback`](-callback/index.md)`?` |
| [controller](controller.md) | `var controller: MediaControllerCompat?`<br>You can set a MediaController It enables to automate media control and setting position |
| [data](data.md) | `var data: `[`WaveFormData`](../-wave-form-data/index.md)`?`<br>WaveFormData show in view |
| [peakMode](peak-mode.md) | `var peakMode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Method to detect blocks height |
| [position](position.md) | `var position: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>position in milliseconds |
| [secPerBlock](sec-per-block.md) | `var secPerBlock: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Duration which a block represent |
| [showTimeText](show-time-text.md) | `var showTimeText: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If you want to hide, set false |
| [textBgColor](text-bg-color.md) | `var textBgColor: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Color used in the text background |
| [textColor](text-color.md) | `var textColor: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Color used in the text |
| [topBlockScale](top-block-scale.md) | `var topBlockScale: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Scale of top blocks |

### Functions

| Name | Summary |
|---|---|
| [onDraw](on-draw.md) | `fun onDraw(canvas: Canvas): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onTouchEvent](on-touch-event.md) | `fun onTouchEvent(event: MotionEvent): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Companion Object Properties

| Name | Summary |
|---|---|
| [PEAKMODE_AVERAGE](-p-e-a-k-m-o-d-e_-a-v-e-r-a-g-e.md) | `const val PEAKMODE_AVERAGE: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Flag to use average in specific range |
| [PEAKMODE_MAX](-p-e-a-k-m-o-d-e_-m-a-x.md) | `const val PEAKMODE_MAX: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Flag to use the maximum value in specific range |
