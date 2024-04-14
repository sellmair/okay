package io.sellmair.okay

import io.sellmair.okay.fs.*
import io.sellmair.okay.serialization.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

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
 * @param key obtained by [OkInput.state]
 * @return the cache record associated with the given input key or `null` if no such record is available
 */
@OptIn(ExperimentalSerializationApi::class)
internal suspend fun OkContext.readCacheRecord(key: OkHash): OkCacheRecord? = withOkContext(okCacheDispatcher) {
    val file = cacheEntriesDirectory.resolve(key.value)
    if (!file.isRegularFile()) return@withOkContext null
    format.decodeFromByteArray<OkCacheRecord>(file.readAll())
        /*
            We should not read entries that have been written during this exact session.
            Otherwise, the UP-TO-DATE checks have a problem:
            Yes, the recently stored cache entry is UP-TO-DATE (because it was just stored),
            but it might not be UP-TO-DATE from the perspective of the previously stored
            coroutine as the dependencies (or outputs) might have changed.
            */
        .takeUnless { it.session == currentOkSessionId() }
}

/**
 * ⚠️Only stores the cache record!!! Consider using [storeCachedCoroutine] instead?
 */
@OptIn(ExperimentalSerializationApi::class)
@OkUnsafe("Consider using ")
internal suspend fun OkContext.storeCacheRecord(value: OkCacheRecord): OkPath = withOkContext(okCacheDispatcher) {
    cacheEntriesDirectory.createDirectories()

    val file = cacheEntriesDirectory.resolve(value.inputHash.value)
    file.write(format.encodeToByteArray(value))
    file
}

