package io.sellmair.okay

import io.sellmair.okay.input.OkInput
import io.sellmair.okay.input.plus
import io.sellmair.okay.output.OkOutput
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class OkCoroutine<T>(
    val key: Deferred<OkHash>,
    val value: Deferred<T>
)

fun <T> OkContext.async(body: suspend OkContext.() -> T): OkAsync<T> {
    val deferred = cs.async { body() }
    return OkAsync { deferred.await() }
}

/**
 * Launches a coroutine with the given [body]:
 * Unlike [cachedCoroutine], this method will not store the output of the calculation of [body] in
 * the .okay cache.
 *
 * However, the coroutine (and the returned [OkAsync] value) will be memoized during the build.
 *
 * ### Typical usages of this method:
 * E.g., parsing some file that is already on disk:
 * It makes no sense to store the output of the read/parse into the cache, as the file we're parsing
 * is already in binary format. In this case, however, we still would like
 * to ensure that this operation is not done over and over and over again:
 *
 * Using this method, we can launch and share a coroutine, which can be shared across the build.
 *
 * ### Using Input.
 * Note: This body can freely call and await other memoized or cached coroutines.
 * It shall declare the [input] it directly uses and will record the input of child coroutines.
 */
suspend fun <T> OkContext.memoizedCoroutine(
    descriptor: OkCoroutineDescriptor<T>, input: OkInput, body: suspend OkContext.() -> T
): T {
    val effectiveInput = descriptor + input
    val coroutine = cs.coroutineContext.okCoroutineCache.getOrPut(effectiveInput) {
        launchOkCoroutine(effectiveInput) { inputHash ->
            val result = withOkStack(descriptor) {
                withOkCoroutineDependencies { OkScope { body() } }
            }

            @OptIn(OkUnsafe::class)
            storeCacheRecord(
                OkCacheRecord(
                    currentOkSessionId(), descriptor, effectiveInput, inputHash, result.dependencies
                )
            )
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

    return coroutine.value.await()
}

/**
 * Will launch a coroutine which will be shared and then cached.
 * If the computation was already done previously, the returned [OkAsync] will be able to provide
 * the value immediately. If an entry in the .okay cache is found, it will be checked and deserialized.
 *
 * @param input The input that this coroutine requires: Note: Inputs from child coroutines do not need
 * to be declared here.
 *
 * @param output The output that this coroutine will store as side effect. This can be a downloaded library file
 * or an entire output directory of a given compilation. Note: Outputs of child coroutines do not need
 * to be declared here.
 *
 */
suspend fun <T> OkContext.cachedCoroutine(
    descriptor: OkCoroutineDescriptor<T>, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): T {
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

    return coroutine.value.await()
}

private fun <T> OkContext.restoreOrLaunchTask(
    descriptor: OkCoroutineDescriptor<T>, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
): OkCoroutine<T> {
    return launchOkCoroutine(input, cs.coroutineContext.pushOkStack(descriptor) + Job()) { key ->
        val cacheResult = tryRestoreCachedCoroutineUnchecked(key)
        cs.coroutineContext[OkCoroutineCacheHook]?.onCacheResult(descriptor, cacheResult)

        @Suppress("UNCHECKED_CAST")
        when (cacheResult) {
            is CacheHit -> cacheResult.entry.outputValue as T
            is CacheMiss -> runCoroutine(key, descriptor, input, output, body)
        }
    }
}

private suspend fun <T> OkContext.runCoroutine(
    inputHash: OkHash,
    descriptor: OkCoroutineDescriptor<T>,
    input: OkInput,
    output: OkOutput,
    body: suspend OkContext.() -> T
): T {
    val resultWithDependencies = withOkCoroutineDependencies {
        OkScope { body() }
    }

    storeCachedCoroutine(
        descriptor, input, inputHash, output, resultWithDependencies.value, resultWithDependencies.dependencies
    )


    return resultWithDependencies.value
}

private fun <T> OkContext.launchOkCoroutine(
    input: OkInput,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    body: suspend (inputHash: OkHash) -> T
): OkCoroutine<T> {
    val key = cs.async { input.currentHash(ctx) }
    return OkCoroutine(
        key = key,
        value = cs.async(coroutineContext, start = CoroutineStart.LAZY) {
            body(key.await())
        }
    )
}
