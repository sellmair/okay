package io.sellmair.okay

import kotlin.coroutines.CoroutineContext

internal interface OkCoroutineCacheHook : CoroutineContext.Element {

    fun onCacheResult(descriptor: OkCoroutineDescriptor<*>, result: CacheResult)

    override val key get() = Key

    companion object Key : CoroutineContext.Key<OkCoroutineCacheHook>
}
