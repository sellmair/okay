package io.sellmair.okay

/**
 * Represents a value which will be available, or become available in the future.
 */
fun interface OkAsync<T> {
    suspend fun await(): T
}

/**
 * Creates a [OkAsync] which already carries the given [value]
 */
fun <T> OkAsync(value: T): OkAsync<T> = OkAsync { value }

suspend fun <T> Iterable<OkAsync<T>>.awaitAll(): List<T> = map { it.await() }

