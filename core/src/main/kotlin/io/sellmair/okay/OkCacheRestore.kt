@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay

import io.sellmair.okay.output.OkOutputDirectory
import io.sellmair.okay.output.OkOutputs
import io.sellmair.okay.utils.*
import kotlin.io.path.*

internal sealed class OkCacheResult

internal data class OkCacheHit(
    val record: OkCacheRecord,
    val upToDate: List<OkCacheRecord> = emptyList(),
    val restored: List<OkCacheRecord> = emptyList()
) : OkCacheResult() {
    override fun toString(): String {
        return "CacheHit(" +
            "upToDate=${upToDate.joinToString { it.descriptor.id }}, " +
            "restored=${restored.joinToString { it.descriptor.id }})"
    }
}

internal data class OkCacheMiss(
    val missing: List<OkHash> = emptyList(),
    val dirty: List<OkCacheRecord> = emptyList()
) : OkCacheResult()

/**
 * The input from the cache entry is not further validated.
 * This is safe to be called if the [cacheKey] was recently created from the current input
 */
internal suspend fun OkContext.tryRestoreCachedCoroutineUnchecked(
    cacheKey: OkHash
): OkCacheResult = withOkContext(okCacheDispatcher) {
    val cacheEntry = readCacheRecord(cacheKey) ?: run {
        return@withOkContext OkCacheMiss(dirty = emptyList())
    }

    withOkStack(cacheEntry.descriptor) {
        tryRestoreCacheRecord(cacheEntry)
    }
}

/**
 * Will only restore the cache from the key, if the inputs were unchanged.
 */
private suspend fun OkContext.tryRestoreCachedCoroutineChecked(cacheKey: OkHash): OkCacheResult {
    val entry = readCacheRecord(cacheKey) ?: return OkCacheMiss(missing = listOf(cacheKey))
    return withOkStack(entry.descriptor) {
        val inputState = entry.input.currentHash(ctx)
        if (inputState != cacheKey) {
            log("Cache miss. Expected: ($cacheKey), found: ($inputState)")
            return@withOkStack OkCacheMiss(dirty = listOf(entry))
        }
        tryRestoreCacheRecord(entry)
    }
}

private suspend fun OkContext.tryRestoreCacheRecord(
    record: OkCacheRecord
): OkCacheResult {
    /* Launch & await restore of dependencies */
    val dependencyResult = record.dependencies.fold<_, OkCacheResult>(OkCacheHit(record)) { result, dependencyKey ->
        result + tryRestoreCachedCoroutineChecked(dependencyKey)
    }

    if (dependencyResult !is OkCacheHit) return dependencyResult
    if (record.output == null) return dependencyResult

    return if (record.output.currentHash(ctx) == record.outputHash) {
        if (record.descriptor.verbosity >= OkCoroutineDescriptor.Verbosity.Info) {
            log("${ansiGreen}UP-TO-DATE${ansiReset} (${record.inputHash}) -> (${record.outputHash})")
        }
        dependencyResult.copy(
            record = record,
            upToDate = dependencyResult.upToDate + record
        )
    } else {
        restoreFilesFromCache(record)
        if (record.descriptor.verbosity >= OkCoroutineDescriptor.Verbosity.Info) {
            log("${ansiYellow}Cache Restored$ansiReset (${record.inputHash}) -> (${record.outputHash})")
        }
        dependencyResult.copy(
            record = record,
            restored = dependencyResult.restored + record
        )
    }
}

private fun OkContext.restoreFilesFromCache(
    entry: OkCacheRecord
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

private operator fun OkCacheResult.plus(other: OkCacheResult): OkCacheResult {
    return when (this) {
        is OkCacheHit -> when (other) {
            is OkCacheMiss -> other
            is OkCacheHit -> copy(
                upToDate = this.upToDate + other.upToDate,
                restored = this.restored + other.restored
            )
        }

        is OkCacheMiss -> when (other) {
            is OkCacheHit -> this
            is OkCacheMiss -> copy(
                missing = this.missing + other.missing,
                dirty = this.dirty + other.dirty
            )
        }
    }
}
