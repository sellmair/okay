package io.sellmair.okay

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

/**
 * The dispatcher used to read/write anything to the cache.
 * Potentially allowing more parallelism could be a performance benefit, but
 * might be hard to proof being correct
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal val okCacheDispatcher = Dispatchers.IO.limitedParallelism(1)

private val OkContext.cacheDirectory
    get() = path(".okay/cache")

internal val OkContext.cacheEntriesDirectory
    get() = cacheDirectory.resolve("entry")

internal val OkContext.cacheBlobsDirectory
    get() = cacheDirectory.resolve("blobs")

/**
 * Read the cache record under the given [key]
 * @param key obtained by [OkInput.cacheKey]
 * @return the cache record associated with the given input key or `null` if no such record is available
 */
internal suspend fun OkContext.readCacheRecord(key: OkHash): OkInputCacheRecord? = withOkContext(okCacheDispatcher) {
    val file = cacheEntriesDirectory.resolve(key.value)
    if (!file.system().isRegularFile()) return@withOkContext null
    ObjectInputStream(file.system().inputStream()).use { stream ->
        stream.readObject() as? OkInputCacheRecord
    }/*
        We should not read entries that have been written during this exact session.
        Otherwise, the UP-TO-DATE checks have a problem:
        Yes, the recently stored cache entry is UP-TO-DATE (because it was just stored),
        but it might not be UP-TO-DATE from the perspective of the previously stored
        coroutine as the dependencies (or outputs) might have changed.
        */
        .takeUnless { it?.session == currentOkSessionId() }
}

/**
 * ⚠️Only stores the cache record!!! Consider using [storeCachedCoroutine] instead?
 */
@OkUnsafe("Consider using ")
internal suspend fun OkContext.storeCacheRecord(value: OkInputCacheRecord): Path = withOkContext(okCacheDispatcher) {
    cacheEntriesDirectory.system().createDirectories()

    val file = cacheEntriesDirectory.resolve(value.key.value).system()
    ObjectOutputStream(file.outputStream().buffered()).use { stream ->
        stream.writeObject(value)
    }

    file
}

