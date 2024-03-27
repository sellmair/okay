package io.sellmair.okay

import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.log

sealed class CacheResult<out T>
data class CacheHit<T>(val entry: OkCacheEntry<T>) : CacheResult<T>()
data object CacheMiss : CacheResult<Nothing>()


/**
 * The input from the cache entry is not further validated.
 * This is safe to be called if the [cacheKey] was recently created from the current input
 */
suspend fun <T> tryRestoreCacheUnchecked(cacheKey: OkHash): CacheResult<T> {
    val cacheEntry = readCacheEntry(cacheKey) ?: run {
        log("Cache Miss ($cacheKey): Missing")
        return CacheMiss
    }

    @Suppress("UNCHECKED_CAST")
    return tryRestoreCache(cacheEntry as OkCacheEntry<T>)
}

/**
 * Will only restore the cache from the key, if the inputs were unchanged.
 */
private suspend fun tryRestoreCacheChecked(cacheKey: OkHash): CacheResult<*>? {
    val entry = readCacheEntry(cacheKey) ?: return null
    return withOkStack(entry.taskDescriptor) {
        val inputState = entry.input.cacheKey()
        if (inputState != cacheKey) {
            log("Cache miss. Expected: ($cacheKey), found: ($inputState)")
            return@withOkStack null
        }
        tryRestoreCache(entry)
    }
}

private suspend fun <T> tryRestoreCache(
    cacheEntry: OkCacheEntry<T>
): CacheResult<T> {
    /* Launch & await restore of dependencies */
    cacheEntry.dependencies.map { dependencyCacheKey ->
        tryRestoreCacheChecked(dependencyCacheKey) ?: run {
            return CacheMiss
        }
    }

    val outputCacheKey = cacheEntry.output.cacheKey()
    if (outputCacheKey == cacheEntry.outputHash) {
        log("${ansiGreen}UP-TO-DATE${ansiReset} (${cacheEntry.key}) -> ($outputCacheKey)")
    } else {
        restoreFilesFromCache(cacheEntry)
        log("Cache Restored (${cacheEntry.key}) -> ($outputCacheKey)")
    }

    return CacheHit(cacheEntry)
}
