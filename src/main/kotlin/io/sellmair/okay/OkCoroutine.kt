package io.sellmair.okay

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class OkCoroutine<T>(
    val key: Deferred<OkHash>,
    val value: Deferred<T>
)

fun <T> OkContext.launchMemoizedCoroutine(
    descriptor: OkCoroutineDescriptor<T>, input: OkInput, body: suspend OkContext.() -> T
): OkAsync<T> {
    val effectiveInput = descriptor + input
    val coroutine = cs.coroutineContext.okCoroutineCache.getOrPut(effectiveInput) {
        launchOkCoroutine(effectiveInput) { key ->
            val result = withOkStack(descriptor) {
                withOkCoroutineDependencies { OkContext { body() } }
            }

            val cacheRecord = OkInputCacheRecordImpl(key, effectiveInput, descriptor, result.dependencies)
            storeCacheRecord(cacheRecord)

            result.value
        }
    }

    /* Bind the dependency to the task! */
    cs.launch {
        bindOkCoroutineDependency(coroutine.key.await())
    }

    /* Ensure the task is started now! */
    cs.launch {
        coroutine.value.await()
    }

    return OkAsync { coroutine.value.await() }
}

fun <T> OkContext.launchCachedCoroutine(
    descriptor: OkCoroutineDescriptor<T>, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkAsync<T> {
    val effectiveInput = descriptor + input
    /* How to bind dependencies from the 'cached' coroutine? */
    /* The async value from okCoroutineCache should return the dependency key to bind to! */
    val coroutine = cs.coroutineContext.okCoroutineCache.getOrPut(effectiveInput) {
        restoreOrLaunchTask(descriptor, effectiveInput, output, body)
    }

    /* Bind the dependency to the new coroutine */
    cs.launch {
        bindOkCoroutineDependency(coroutine.key.await())
    }

    /* Ensure the coroutine is starting */
    cs.launch {
        coroutine.value.await()
    }

    return OkAsync { coroutine.value.await() }
}

private fun <T> OkContext.restoreOrLaunchTask(
    descriptor: OkCoroutineDescriptor<T>, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkCoroutine<T> {
    return launchOkCoroutine(input, cs.coroutineContext.pushOkStack(descriptor) + Job()) { key ->
        @Suppress("UNCHECKED_CAST")
        when (val cacheResult = tryRestoreCacheUnchecked(key)) {
            is CacheHit -> (cacheResult.entry as OkOutputCacheRecord<*>).value as T
            is CacheMiss -> runTask(key, descriptor, input, output, body)
        }
    }
}

private suspend fun <T> runTask(
    inputCacheKey: OkHash,
    descriptor: OkCoroutineDescriptor<T>,
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

private fun <T> OkContext.launchOkCoroutine(
    input: OkInput,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    body: suspend (key: OkHash) -> T
): OkCoroutine<T> {
    val key = cs.async { input.cacheKey() }
    return OkCoroutine(
        key = key,
        value = cs.async(coroutineContext, start = CoroutineStart.LAZY) {
            body(key.await())
        }
    )
}
