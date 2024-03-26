package io.sellmair.okay

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

internal class OkCoroutineResultWithDependencies<T>(
    val value: T, val dependencies: List<OkHash>
)

suspend fun bindOkCoroutineDependency(key: OkHash) {
    val dependencyBinder = currentCoroutineContext()[OkCoroutineDependencyBinder]
    dependencyBinder?.bind(key)
}

internal suspend fun <T> withOkCoroutineDependencies(body: suspend () -> T): OkCoroutineResultWithDependencies<T> {
    val newBinder = OkCoroutineDependencyBinder()
    val result = withContext(newBinder) {
        body()
    }

    return OkCoroutineResultWithDependencies(result, newBinder.dependencies())
}

private class OkCoroutineDependencyBinder : CoroutineContext.Element {
    private val dependencies = mutableListOf<OkHash>()
    private val lock = ReentrantLock()

    fun bind(cacheKey: OkHash) {
        lock.withLock {
            dependencies.add(cacheKey)
        }
    }

    fun dependencies() = lock.withLock { dependencies.toList() }

    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkCoroutineDependencyBinder>
}
