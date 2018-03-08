[space.siy.waveformview](../index.md) / [WaveFormData](./index.md)

# WaveFormData

`class WaveFormData : Parcelable`

Data object contains raw sample data of 16bit short array and some params

You have to use [WaveFormData.Factory](-factory/index.md) to build

### Types

| Name | Summary |
|---|---|
| [CREATOR](-c-r-e-a-t-o-r/index.md) | `companion object CREATOR : Creator<`[`WaveFormData`](./index.md)`>` |
| [Factory](-factory/index.md) | `class Factory`<br>Factory class to build [WaveFormData](./index.md) |

### Properties

| Name | Summary |
|---|---|
| [channel](channel.md) | `val channel: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Count of channel |
| [duration](duration.md) | `val duration: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Duration of data in milliseconds |
| [sampleRate](sample-rate.md) | `val sampleRate: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Sample rate of data |
| [samples](samples.md) | `var samples: `[`ShortArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-short-array/index.html)<br>Raw sample data |

### Functions

| Name | Summary |
|---|---|
| [describeContents](describe-contents.md) | `fun describeContents(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](write-to-parcel.md) | `fun writeToParcel(parcel: Parcel, flags: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [createFromParcel](create-from-parcel.md) | `fun createFromParcel(parcel: Parcel): `[`WaveFormData`](./index.md) |
| [newArray](new-array.md) | `fun newArray(size: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`WaveFormData`](./index.md)`?>` |
