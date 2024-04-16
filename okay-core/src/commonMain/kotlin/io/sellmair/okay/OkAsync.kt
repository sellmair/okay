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
fun <T> OkAsync(value: T): io.sellmair.okay.OkAsync<T> = OkAsync { value }

suspend fun <T> Iterable<io.sellmair.okay.OkAsync<T>>.awaitAll(): List<T> = map { it.await() }

