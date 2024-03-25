package io.sellmair.okay

import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

fun runOkay(block: suspend OkContext.() -> Unit) {
    val context = OkContextImpl(CoroutineScope(SupervisorJob() + Dispatchers.Default))
    context.runBlocking(block)
}

data class OkContextImpl(
    private val cs: CoroutineScope,
    override val stack: List<String> = emptyList(),
    private val values: HashMap<OkInput, OkAsync<*>> = HashMap(),
    private val cacheRestores: HashMap<OkInput, OkAsync<*>> = HashMap()
) : OkContext {

    fun runBlocking(block: suspend OkContextImpl.() -> Unit) {
        runBlocking(cs.coroutineContext + OkAsyncBinder()) {
            block()
        }
    }

    private val lock = ReentrantLock()

    override fun log(value: String) {
        println("[${stack.joinToString("/")}]: $value")
    }

    override fun <T> launchTask(
        title: String,
        input: OkInput,
        output: OkOutput,
        body: suspend OkContext.() -> T
    ): OkAsync<T> {
        @Suppress("UNCHECKED_CAST")
        return lock.withLock {
            values.getOrPut(input) new@{
                val newCtx = copy(
                    stack = stack + title,
                    cs = CoroutineScope(cs.coroutineContext + Job())
                )

                newCtx.createNewAsync(title, input, output, body)
            }
        } as OkAsync<T>
    }

    private fun <T> createNewAsync(
        title: String, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
    ): OkAsync<T> {
        log("requested '$title'")

        val deferred = cs.async {
            val inputKey = input.cacheKey()
            when (val cacheResult = tryRestoreCache<T>(input, inputKey)) {
                is CacheHit -> cacheResult.entry
                is CacheMiss -> {
                    val binder = OkAsyncBinder()
                    withContext(binder) {
                        val result = body()
                        log("Storing cache ($inputKey)")
                        storeCache(inputKey, result, title, input, output, binder.dependencies.map { it.key })
                    }
                }
            }
        }

        return OkDeferred(deferred)
    }

    private suspend fun <T> tryRestoreCache(
        input: OkInput, cacheKey: OkHash = input.cacheKey()
    ): CacheResult<T> {
        val cacheEntry = readCacheEntry(cacheKey) ?: run {
            log("Cache Miss ($cacheKey): Missing")
            return CacheMiss(cacheKey)
        }

        /* Launch & await restore of dependencies */
        cacheEntry.dependencies.map { dependencyCacheKey ->
            tryRestoreCache(dependencyCacheKey) ?: run {
                return CacheMiss(cacheKey)
            }
        }

        val outputCacheKey = cacheEntry.output.cacheKey()
        if (outputCacheKey == cacheEntry.outputHash) {
            log("UP-TO-DATE ($cacheKey) -> ($outputCacheKey)")
        } else {
            restoreFilesFromCache(cacheEntry)
            log("Cache Restored ($cacheKey) -> ($outputCacheKey)")
        }

        @Suppress("UNCHECKED_CAST")
        return CacheHit(cacheEntry as OkCacheEntry<T>)
    }

    private suspend fun tryRestoreCache(cacheKey: OkHash): CacheResult<*>? {
        val entry = readCacheEntry(cacheKey) ?: return null
        val inputState = entry.input.cacheKey()
        if (inputState != cacheKey) {
            log("Cache miss: '${entry.title}. Expected: ($cacheKey), found: ($inputState)")
            return null
        }
        return tryRestoreCache<Any?>(entry.input, cacheKey)
    }

    private class OkAsyncBinder : CoroutineContext.Element {
        val dependencies = mutableListOf<OkCacheEntry<*>>()
        override val key: CoroutineContext.Key<*> = Key

        companion object Key : CoroutineContext.Key<OkAsyncBinder>
    }

    override suspend fun <T> await0(async: OkAsync<T>): T {
        if (async !is OkBindableAsync<T>) error("Expected '$async' to be bindable")
        val binder = currentCoroutineContext()[OkAsyncBinder] ?: error("Missing 'OkAsyncBinder'")
        val cacheEntry = async.await()
        binder.dependencies.add(cacheEntry)
        return cacheEntry.value
    }

    interface OkBindableAsync<T> : OkAsync<T> {
        suspend fun await(): OkCacheEntry<T>
    }

    private class OkDeferred<T>(
        val deferred: Deferred<OkCacheEntry<T>>
    ) : OkBindableAsync<T> {
        override suspend fun await(): OkCacheEntry<T> {
            return deferred.await()
        }
    }
}


sealed class CacheResult<out T>

data class CacheHit<T>(val entry: OkCacheEntry<T>) : CacheResult<T>()
data class CacheMiss(val key: OkHash) : CacheResult<Nothing>()
