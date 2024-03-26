package io.sellmair.okay

import kotlinx.coroutines.*

fun <T> OkContext.cachedTask(
    title: String, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkAsync<T> {
    return cs.coroutineContext.okCoroutineCache.getOrPut(input) {
        cs.restoreOrLaunchTask(title, input, output, body)
    }
}

private fun <T> CoroutineScope.restoreOrLaunchTask(
    title: String, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkAsync<T> {
    val deferred = async(coroutineContext.pushOkStack(title) + Job()) {
        coroutineScope scope@{
            val inputCacheKey = input.cacheKey()
            bindOkCoroutineDependency(inputCacheKey)

            when (val cacheResult = tryRestoreCacheUnchecked<T>(inputCacheKey)) {
                is CacheHit -> cacheResult.entry.value
                is CacheMiss -> runTask(inputCacheKey, title, input, output, body)
            }
        }
    }

    return OkAsync { deferred.await() }
}

private suspend fun <T> runTask(
    inputCacheKey: OkHash, title: String, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): T {
    val resultWithDependencies = withOkCoroutineDependencies {
        OkContext { body() }
    }

    storeCache(
        inputCacheKey, resultWithDependencies.value, title, input, output, resultWithDependencies.dependencies
    )

    return resultWithDependencies.value
}
