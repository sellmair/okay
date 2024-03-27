package io.sellmair.okay

import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.log

internal sealed class CacheResult
internal data class CacheHit(val entry: OkInputCacheRecord) : CacheResult()
internal data object CacheMiss : CacheResult()


/**
 * The input from the cache entry is not further validated.
 * This is safe to be called if the [cacheKey] was recently created from the current input
 */
internal suspend fun tryRestoreCacheUnchecked(cacheKey: OkHash): CacheResult {
    val cacheEntry = readCacheEntry(cacheKey) ?: run {
        log("Cache Miss ($cacheKey): Missing")
        return CacheMiss
    }

    return tryRestoreCache(cacheEntry)
}

/**
 * Will only restore the cache from the key, if the inputs were unchanged.
 */
private suspend fun tryRestoreCacheChecked(cacheKey: OkHash): CacheResult? {
    val entry = readCacheEntry(cacheKey) ?: return null
    return withOkStack(entry.descriptor) {
        val inputState = entry.input.cacheKey()
        if (inputState != cacheKey) {
            log("Cache miss. Expected: ($cacheKey), found: ($inputState)")
            return@withOkStack null
        }
        tryRestoreCache(entry)
    }
}

private suspend fun tryRestoreCache(
    cacheEntry: OkInputCacheRecord
): CacheResult {
    /* Launch & await restore of dependencies */
    cacheEntry.dependencies.map { dependencyCacheKey ->
        tryRestoreCacheChecked(dependencyCacheKey) ?: run {
            return CacheMiss
        }
    }

    if (cacheEntry is OkOutputCacheRecord<*>) {
        val outputCacheKey = cacheEntry.output.cacheKey()
        if (outputCacheKey == cacheEntry.outputHash) {
            log("${ansiGreen}UP-TO-DATE${ansiReset} (${cacheEntry.key}) -> ($outputCacheKey)")
        } else {
            restoreFilesFromCache(cacheEntry)
            log("Cache Restored (${cacheEntry.key}) -> ($outputCacheKey)")
        }
    }
    return CacheHit(cacheEntry)
}
