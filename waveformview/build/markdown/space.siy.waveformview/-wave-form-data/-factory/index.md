[space.siy.waveformview](../../index.md) / [WaveFormData](../index.md) / [Factory](./index.md)

# Factory

`class Factory`

Factory class to build [WaveFormData](../index.md)

Note : It build data asynchronously

### Types

| Name | Summary |
|---|---|
| [Callback](-callback/index.md) | `interface Callback`<br>Provide callbacks indicating progress to the user. |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Factory(afd: AssetFileDescriptor)`<br>Sets the data source (AssetFileDescriptor) to use. It is the caller's responsibility to close the file descriptor. It is safe to do so as soon as this call returns.`Factory(context: Context, uri: Uri, headers: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`<br>Sets the data source as a content Uri.`Factory(fd: `[`FileDescriptor`](http://docs.oracle.com/javase/6/docs/api/java/io/FileDescriptor.html)`)`<br>Sets the data source (FileDescriptor) to use. It is the caller's responsibility to close the file descriptor. It is safe to do so as soon as this call returns.`Factory(dataSource: MediaDataSource)`<br>Sets the data source (MediaDataSource) to use.`Factory(fd: `[`FileDescriptor`](http://docs.oracle.com/javase/6/docs/api/java/io/FileDescriptor.html)`, offset: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, length: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`)`<br>Sets the data source (FileDescriptor) to use. The FileDescriptor must be seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility to close the file descriptor. It is safe to do so as soon as this call returns.`Factory(path: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`<br>`Factory(path: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, headers: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`<br>Sets the data source (file-path or http URL) to use. |

### Functions

| Name | Summary |
|---|---|
| [build](build.md) | `fun build(callback: `[`Callback`](-callback/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Build a data using constructor params |
