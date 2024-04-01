package io.sellmair.okay

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

internal val CoroutineContext.okCoroutineCache: OkCoroutineCache
    get() = this[OkCoroutineCache] ?: error("Missing 'OkCoroutineCache'")

internal class OkCoroutineCache : CoroutineContext.Element {
    private val lock = ReentrantLock()
    private val values = HashMap<OkInput, OkCoroutine<*>>()
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkCoroutineCache>

    fun <T> getOrPut(input: OkInput, create: () -> OkCoroutine<T>): OkCoroutine<T> {
        @Suppress("UNCHECKED_CAST")
        return lock.withLock {
            values.getOrPut(input, create)
        } as OkCoroutine<T>
    }
}
