package io.sellmair.okay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

data class OkBuildContextImpl(
    private val scope: CoroutineScope,
    private val paths: List<String> = emptyList(),
    private val values: ConcurrentHashMap<OkInput, OkAsync<*>> = ConcurrentHashMap<OkInput, OkAsync<*>>()
) : OkBuildContext {

    override fun log(value: String) {
        println("[${paths.joinToString("/")}]: $value")
    }

    override fun <T> cached(
        title: String,
        input: OkInput,
        output: OkOutput,
        body: suspend OkBuildContext.() -> T
    ): OkAsync<T> {
        @Suppress("UNCHECKED_CAST")
        return values.computeIfAbsent(input) compute@{
            with(copy(paths = paths + title)) {
                val cacheKey = input.cacheKey()
                val cacheEntry = readCacheEntry(cacheKey)

                if (cacheEntry == null) {
                    log("Missing cache entry for ${cacheKey.value.take(6)}")
                }

                if (cacheEntry != null) {
                    log("Checking cache ${cacheKey.value.take(6)}")
                    if (output.cacheKey() == cacheEntry.outputHash) {
                        log("UP-TO-DATE (${cacheEntry.outputHash.value.take(6)})")
                        val value = cacheEntry.value
                        return@compute object : OkAsync<T> {
                            override suspend fun await(): T {
                                restoreFilesFromCache(cacheEntry)
                                return value as T
                            }
                        }
                    } else {
                        log("Cache Miss: Expected ${output.cacheKey()}, found: ${cacheEntry.outputHash}")
                    }
                }

                val async = scope.async {
                    runCatching {
                        with(copy(paths = paths + title)) {
                            body()
                        }
                    }
                }

                scope.launch {
                    val result = async.await()
                    if (result.isSuccess) {
                        log("Storing cache...")
                        storeCache(cacheKey, result.getOrThrow(), output)
                        log("Stored cache")
                    }
                }

                object : OkAsync<T> {
                    override suspend fun await(): T {
                        return async.await().getOrThrow()
                    }
                }
            }
        } as OkAsync<T>
    }

}