package io.sellmair.okay

import org.jetbrains.annotations.Async

fun interface OkAsync<T> {
    suspend fun await(): T
}

suspend fun <T> Iterable<OkAsync<T>>.awaitAll(): List<T> = map { it.await() }
