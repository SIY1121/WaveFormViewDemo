[space.siy.waveformview](../../index.md) / [WaveFormData](../index.md) / [Factory](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`Factory(afd: AssetFileDescriptor)`

Sets the data source (AssetFileDescriptor) to use.
It is the caller's responsibility to close the file descriptor.
It is safe to do so as soon as this call returns.

### Parameters

`afd` - the AssetFileDescriptor for the file you want to extract from.

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.`Factory(context: Context, uri: Uri, headers: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`

Sets the data source as a content Uri.

### Parameters

`context` - the Context to use when resolving the Uri

`uri` - the Content URI of the data you want to extract from.

`headers` - the headers to be sent together with the request for the data. This can be null if no specific headers are to be sent with the request.

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.`Factory(fd: `[`FileDescriptor`](http://docs.oracle.com/javase/6/docs/api/java/io/FileDescriptor.html)`)`

Sets the data source (FileDescriptor) to use.
It is the caller's responsibility to close the file descriptor.
It is safe to do so as soon as this call returns.

### Parameters

`fd` - the FileDescriptor for the file you want to extract from.

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.`Factory(dataSource: MediaDataSource)`

Sets the data source (MediaDataSource) to use.

### Parameters

`dataSource` - the MediaDataSource for the media you want to extract from

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.`Factory(fd: `[`FileDescriptor`](http://docs.oracle.com/javase/6/docs/api/java/io/FileDescriptor.html)`, offset: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, length: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`)`

Sets the data source (FileDescriptor) to use.
The FileDescriptor must be seekable (N.B. a LocalSocket is not seekable).
It is the caller's responsibility to close the file descriptor.
It is safe to do so as soon as this call returns.

### Parameters

`fd` - the FileDescriptor for the file you want to extract from.

`offset` - the offset into the file where the data to be extracted starts, in bytes

`length` - the length in bytes of the data to be extracted

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.`Factory(path: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`

Sets the data source (file-path or http URL) to use.

### Parameters

`path` - the path of the file, or the http URL of the stream
When path refers to a local file, the file may actually be opened by a process other than the calling application.
This implies that the pathname should be an absolute path (as any other process runs with unspecified current working directory),
and that the pathname should reference a world-readable file.
As an alternative, the application could first open the file for reading,
and then use the file descriptor form setDataSource(FileDescriptor).

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.`Factory(path: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, headers: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`

Sets the data source (file-path or http URL) to use.

### Parameters

`path` - the path of the file, or the http URL
When path refers to a network file the android.Manifest.permission.INTERNET permission is required.

`headers` - the headers associated with the http request for the stream you want to play.
This can be null if no specific headers are to be sent with the request.

### Exceptions

`Exception` - If media doesn't have audio track, throw exception.