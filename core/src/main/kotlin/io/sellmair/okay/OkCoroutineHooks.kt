package io.sellmair.okay

import kotlin.coroutines.CoroutineContext

internal interface OkCoroutineCacheHook : CoroutineContext.Element {

    fun onCacheResult(descriptor: OkCoroutineDescriptor<*>, result: OkCacheResult)

    override val key get() = Key

    companion object Key : CoroutineContext.Key<OkCoroutineCacheHook>
}
