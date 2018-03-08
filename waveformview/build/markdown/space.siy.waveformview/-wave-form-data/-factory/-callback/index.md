[space.siy.waveformview](../../../index.md) / [WaveFormData](../../index.md) / [Factory](../index.md) / [Callback](./index.md)

# Callback

`interface Callback`

Provide callbacks indicating progress to the user.

### Functions

| Name | Summary |
|---|---|
| [onComplete](on-complete.md) | `abstract fun onComplete(waveFormData: `[`WaveFormData`](../../index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called when complete Take out your data built |
| [onProgress](on-progress.md) | `abstract fun onProgress(progress: `[`Float`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called when has progress You can indicate progress to user using ProgressBar |
