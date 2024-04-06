package io.sellmair.okay.utils

import io.sellmair.okay.*
import io.sellmair.okay.OkCacheMiss
import io.sellmair.okay.OkCacheResult
import io.sellmair.okay.OkCoroutineCacheHook
import io.sellmair.okay.io.OkPath
import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.test.fail

internal class OkTestCoroutineCacheHook : OkCoroutineCacheHook {
    private val records = mutableListOf<Record>()
    private val lock = ReentrantLock()

    data class Record(val descriptor: OkCoroutineDescriptor<*>, val result: OkCacheResult)

    override fun onCacheResult(descriptor: OkCoroutineDescriptor<*>, result: OkCacheResult) {
        lock.withLock { records.add(Record(descriptor, result)) }
    }

    fun records() = lock.withLock { records.toList() }

    fun clearRecords() = lock.withLock { records.clear() }
}

internal suspend fun cacheRecords(): List<OkTestCoroutineCacheHook.Record> =
    (currentCoroutineContext()[OkCoroutineCacheHook] as OkTestCoroutineCacheHook).records()

internal suspend fun clearCacheRecords() =
    (currentCoroutineContext()[OkCoroutineCacheHook] as OkTestCoroutineCacheHook).clearRecords()

internal suspend fun assertCacheMiss(module: OkPath, id: String): OkCacheMiss {
    val record = assertCacheRecord(module, id)
    if (record.result !is OkCacheMiss) {
        fail("Expected CacheMiss in module '$module' for '$id'")
    }

    return record.result
}

internal suspend fun assertCacheUpToDate(module: OkPath, id: String) {
    val record = assertCacheRecord(module, id)
    when (val result = record.result) {
        is OkCacheMiss -> fail("Expected CacheHit in module '$module' for '$id'. Dirty: ${result.dirty}")
        is OkCacheHit -> if (result.restored.isNotEmpty()) {
            fail("Expected 'UP-TO-DATE' in module '$module' for '$id'. Found $result")
        }
    }
}

internal suspend fun assertCacheRestored(module: OkPath, id: String) {
    val record = assertCacheRecord(module, id)
    when (val result = record.result) {
        is OkCacheMiss -> fail("Expected CacheHit in module '$module' for '$id'. Dirty: ${result.dirty}")
        is OkCacheHit -> {
            if (result.restored.none { it.descriptor.id == id }) {
                fail("Expected $id to be 'restored'. Found $result")
            }
        }
    }
}


internal suspend fun assertCacheRecord(module: OkPath, id: String): OkTestCoroutineCacheHook.Record {
    val matches = cacheRecords()
        .filter { it.descriptor.module == module && it.descriptor.id == id }

    if (matches.isEmpty()) {
        fail("No matching cache records for module '$module' and id '$id'")
    }

    if (matches.size > 1) {
        fail("Duplicate cache records for module '$module' and id '$id': $matches")
    }

    return matches.single()
}