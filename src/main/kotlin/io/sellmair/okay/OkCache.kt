@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay

import io.sellmair.okay.utils.withClosure
import kotlinx.coroutines.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import kotlin.io.path.*

@OptIn(ExperimentalCoroutinesApi::class)
private val okCacheDispatcher = Dispatchers.IO.limitedParallelism(1)

private val OkContext.cacheDirectory
    get() = path(".okay/cache")

private val OkContext.cacheEntriesDirectory
    get() = cacheDirectory.resolve("entry")

private val OkContext.cacheBlobsDirectory
    get() = cacheDirectory.resolve("blobs")

internal suspend fun OkContext.readCacheEntry(key: OkHash): OkInputCacheRecord? = withOkContext(okCacheDispatcher) {
    val file = cacheEntriesDirectory.resolve(key.value)
    if (!file.system().isRegularFile()) return@withOkContext null
    ObjectInputStream(file.system().inputStream()).use { stream ->
        stream.readObject() as? OkInputCacheRecord
    }
}

suspend fun <T> OkContext.storeCache(
    key: OkHash,
    value: T,
    taskDescriptor: OkCoroutineDescriptor<T>,
    input: OkInput,
    output: OkOutput,
    dependencies: Iterable<OkHash>
): OkOutputCacheRecord<T> = withOkContext(Dispatchers.IO) {
    cacheBlobsDirectory.system().createDirectories()
    val outputHash = output.cacheKey()

    val files = output.walkFiles()
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
        descriptor = taskDescriptor,
        input = input,
        output = output,
        outputHash = outputHash,
        dependencies = dependencies.toSet(),
        outputState = files
    )

    storeCacheRecord(entry)
    entry
}

/**
 * ⚠️Only stores the cache record!!! Consider using [storeCache] instead?
 */
internal suspend fun OkContext.storeCacheRecord(value: OkInputCacheRecord): Path = withOkContext(okCacheDispatcher) {
    cacheEntriesDirectory.system().createDirectories()

    val file = cacheEntriesDirectory.resolve(value.key.value).system()
    ObjectOutputStream(file.outputStream().buffered()).use { stream ->
        stream.writeObject(value)
    }

    file
}

suspend fun OkContext.restoreFilesFromCache(entry: OkOutputCacheRecord<*>): Unit = withOkContext(okCacheDispatcher) {
    entry.output.withClosure { output -> if (output is OkOutputs) output.values else emptyList() }
        .filterIsInstance<OkOutputDirectory>()
        .forEach { outputDirectory -> outputDirectory.path.system().deleteRecursively() }

    entry.outputState.forEach { (path, hash) ->
        val blob = cacheBlobsDirectory.resolve(hash.value).system()
        if (blob.isRegularFile()) {
            path.system().createParentDirectories()
            blob.copyTo(path.system(), true)
        }
    }
}


suspend fun OkOutput.cacheKey(): OkHash = withContext(okCacheDispatcher) {
    when (this@cacheKey) {
        is OkOutputs -> hash(values.map { it.cacheKey() })
        is OkEmptyOutput -> hash("")
        is OkOutputDirectory -> path.system().directoryCacheKey()
        is OkOutputFile -> path.system().regularFileCacheKey()
    }
}


internal suspend fun Path.directoryCacheKey(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        if (isDirectory()) {
            listDirectoryEntries().map { entry ->
                if (entry.isDirectory()) {
                    push(entry.directoryCacheKey())
                } else if (entry.isRegularFile()) {
                    push(entry.regularFileCacheKey())
                }
            }
        }
    }
}

internal suspend fun Path.regularFileCacheKey(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        push(if (exists()) 1 else 0)

        if (isRegularFile()) {
            val buffer = ByteArray(2048)
            inputStream().buffered().use { input ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    push(buffer, 0, read)
                }
            }
        }
    }
}
