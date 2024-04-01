package io.sellmair.okay

import kotlinx.coroutines.Dispatchers
import java.nio.file.FileAlreadyExistsException
import kotlin.io.path.*


/**
 * Stores a [OkOutputCacheRecord] to the given input [key]
 * @param key The current state of the [input]
 * @param value The result of the cached coroutine
 * @param descriptor The descriptor of the cached coroutine
 * @param input The input of the cached coroutine
 * @param output The output of the cached coroutine. This output will be traversed and all files associated
 * with the output will be captured in the [cacheBlobsDirectory]
 * @param dependencies The dependencies of the coroutine
 */
suspend fun <T> OkContext.storeCachedCoroutine(
    key: OkHash,
    value: T,
    descriptor: OkCoroutineDescriptor<T>,
    input: OkInput,
    output: OkOutput,
    dependencies: Iterable<OkHash>
): OkOutputCacheRecord<T> = withOkContext(Dispatchers.IO) {
    cacheBlobsDirectory.system().createDirectories()
    val outputHash = output.cacheKey()

    /**
     * See [OkOutputCacheRecord.outputSnapshot]
     */
    val outputSnapshot = output.walkFiles()
        .toList()
        .mapNotNull { path ->
            if (!path.exists() || !path.isRegularFile()) return@mapNotNull null
            val fileCacheKey = path.regularFileCacheKey()
            val blobFile = cacheBlobsDirectory.resolve(fileCacheKey.value).system()

            try {
                blobFile.createFile()
                path.copyTo(blobFile, true)
            } catch (t: FileAlreadyExistsException) {
                /* File exists, no need to store it */
            }

            path.ok() to fileCacheKey
        }
        .toMap()

    val entry = OkOutputCacheRecord(
        key = key,
        value = value,
        descriptor = descriptor,
        input = input,
        output = output,
        outputHash = outputHash,
        dependencies = dependencies.toSet(),
        outputSnapshot = outputSnapshot
    )
    @OptIn(OkUnsafe::class)
    storeCacheRecord(entry)
    entry
}
