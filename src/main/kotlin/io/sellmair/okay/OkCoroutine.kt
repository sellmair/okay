package io.sellmair.okay

import kotlinx.coroutines.*

fun <T> OkContext.cachedTask(
    descriptor: OkTaskDescriptor<T>, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkAsync<T> {
    val effectiveInput = descriptor + input
    return cs.coroutineContext.okCoroutineCache.getOrPut(effectiveInput) {
        cs.restoreOrLaunchTask(descriptor, effectiveInput, output, body)
    }
}

private fun <T> CoroutineScope.restoreOrLaunchTask(
    descriptor: OkTaskDescriptor<T>, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkAsync<T> {
    val deferred = async(coroutineContext.pushOkStack(descriptor.title) + Job()) {
        coroutineScope scope@{
            val inputCacheKey = input.cacheKey()
            bindOkCoroutineDependency(inputCacheKey)

            when (val cacheResult = tryRestoreCacheUnchecked<T>(inputCacheKey)) {
                is CacheHit -> cacheResult.entry.value
                is CacheMiss -> runTask(inputCacheKey, descriptor, input, output, body)
            }
        }
    }

    return OkAsync { deferred.await() }
}

private suspend fun <T> runTask(
    inputCacheKey: OkHash,
    descriptor: OkTaskDescriptor<T>,
    input: OkInput,
    output: OkOutput,
    body: suspend OkContext.() -> T
): T {
    val resultWithDependencies = withOkCoroutineDependencies {
        OkContext { body() }
    }

    storeCache(
        inputCacheKey, resultWithDependencies.value, descriptor, input, output, resultWithDependencies.dependencies
    )

    return resultWithDependencies.value
}
