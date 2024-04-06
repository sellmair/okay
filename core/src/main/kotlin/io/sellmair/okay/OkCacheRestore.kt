@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay

import io.sellmair.okay.output.OkOutputDirectory
import io.sellmair.okay.output.OkOutputs
import io.sellmair.okay.utils.*
import kotlin.io.path.*

internal sealed class CacheResult
internal data class CacheHit(val entry: OkCacheRecord<*>) : CacheResult()
internal data class CacheMiss(val dirty: OkCacheRecord<*>?) : CacheResult()

/**
 * The input from the cache entry is not further validated.
 * This is safe to be called if the [cacheKey] was recently created from the current input
 */
internal suspend fun OkContext.tryRestoreCachedCoroutineUnchecked(
    cacheKey: OkHash
): CacheResult = withOkContext(okCacheDispatcher) {
    val cacheEntry = readCacheRecord(cacheKey) ?: run {
        return@withOkContext CacheMiss(null)
    }

    withOkStack(cacheEntry.descriptor) {
        tryRestoreCacheRecord(cacheEntry)
    }
}

/**
 * Will only restore the cache from the key, if the inputs were unchanged.
 */
private suspend fun OkContext.tryRestoreCachedCoroutineChecked(cacheKey: OkHash): CacheResult {
    val entry = readCacheRecord(cacheKey) ?: return CacheMiss(null)
    return withOkStack(entry.descriptor) {
        val inputState = entry.input.currentHash(ctx)
        if (inputState != cacheKey) {
            log("Cache miss. Expected: ($cacheKey), found: ($inputState)")
            return@withOkStack CacheMiss(entry)
        }
        tryRestoreCacheRecord(entry)
    }
}

private suspend fun OkContext.tryRestoreCacheRecord(
    cacheEntry: OkCacheRecord<*>
): CacheResult {
    /* Launch & await restore of dependencies */
    cacheEntry.dependencies.map { dependencyCacheKey ->
        val restoredDependencyResult = tryRestoreCachedCoroutineChecked(dependencyCacheKey)
        if (restoredDependencyResult is CacheMiss) {
            return restoredDependencyResult
        }
    }

    if (cacheEntry.output != null) {
        if (cacheEntry.output.currentHash(ctx) != cacheEntry.outputHash) {
            if (cacheEntry.descriptor.verbosity >= OkCoroutineDescriptor.Verbosity.Info) {
                log("${ansiGreen}UP-TO-DATE${ansiReset} (${cacheEntry.inputHash}) -> (${cacheEntry.outputHash})")
            }
        } else {
            restoreFilesFromCache(cacheEntry)
            if (cacheEntry.descriptor.verbosity >= OkCoroutineDescriptor.Verbosity.Info) {
                log("${ansiYellow}Cache Restored$ansiReset (${cacheEntry.inputHash}) -> (${cacheEntry.outputHash})")
            }
        }
    }
    return CacheHit(cacheEntry)
}

private fun OkContext.restoreFilesFromCache(
    entry: OkCacheRecord<*>
) {
    entry.output.withClosure { output -> if (output is OkOutputs) output.values else emptyList() }
        .filterIsInstance<OkOutputDirectory>()
        .forEach { outputDirectory -> outputDirectory.path.system().deleteRecursively() }

    entry.outputFiles.orEmpty().forEach { (path, hash) ->
        val blob = cacheBlobsDirectory.resolve(hash.value).system()
        if (blob.isRegularFile()) {
            path.system().createParentDirectories()
            blob.copyTo(path.system(), true)
        }
    }
}
