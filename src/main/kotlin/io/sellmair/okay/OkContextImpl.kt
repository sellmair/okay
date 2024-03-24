package io.sellmair.okay

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

data class OkContextImpl(
    private val scope: CoroutineScope,
    private val paths: List<String> = emptyList(),
    private val values: ConcurrentHashMap<OkInput, OkAsync<*>> = ConcurrentHashMap<OkInput, OkAsync<*>>()
) : OkContext {

    override fun log(value: String) {
        println("[${paths.joinToString("/")}]: $value")
    }

    override fun <T> cached(
        title: String,
        input: OkInput,
        output: OkOutput,
        body: suspend OkContext.() -> T
    ): OkAsync<T> {
        @Suppress("UNCHECKED_CAST")
        return values.computeIfAbsent(input) compute@{
            with(copy(paths = paths + title)) {
                createNewAsync(input, output, body)
            }
        } as OkAsync<T>
    }

    private fun <T> createNewAsync(
        input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
    ): OkAsync<T> {
        /* Hash all inputs to get a key for the calculation */
        val cacheKey = input.cacheKey()

        /* Load the cache entry for the given cache key which might be available on disk */
        val cacheEntry = readCacheEntry(cacheKey)
        if (cacheEntry == null) {
            log("Missing cache entry for ${cacheKey.value.take(6)}")
        }

        if (cacheEntry != null) {
            /* If the outputs are still OK from the previously stored calculation, then we can re-use the cache entry */
            log("Checking cache ${cacheKey.value.take(6)}")
            if (output.cacheKey() == cacheEntry.outputHash) {
                log("UP-TO-DATE (${cacheEntry.outputHash.value.take(6)})")
                return OkAsyncImpl(scope.async {
                    restoreFilesFromCache(cacheEntry)
                    @Suppress("UNCHECKED_CAST")
                    cacheEntry.value as T
                })

            } else {
                log("Cache Miss: Expected ${output.cacheKey()}, found: ${cacheEntry.outputHash}")
            }
        }

        /* No cache hit: Let's start the operation! */
        val asyncResult = scope.async {
            val result = runCatching {
                body()
            }

            /* In case of the operation being successful: Store it to the persistent cache */
            if (result.isSuccess) {
                log("Storing cache...")
                storeCache(cacheKey, result.getOrThrow(), output)
                log("Stored cache")
            }

            result
        }

        return OkAsyncImpl(scope.async {
            asyncResult.await().getOrThrow()
        })
    }
}

private class OkAsyncImpl<T>(
    private val deferred: Deferred<T>,
) : OkAsync<T> {
    override suspend fun await(): T {
        return deferred.await()
    }
}