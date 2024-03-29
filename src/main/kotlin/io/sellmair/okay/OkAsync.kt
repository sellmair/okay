package io.sellmair.okay

fun interface OkAsync<T> {
    suspend fun await(): T
}

fun <T> OkAsync(value: T): OkAsync<T> = OkAsync { value }

suspend fun <T> Iterable<OkAsync<T>>.awaitAll(): List<T> = map { it.await() }

