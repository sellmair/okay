package io.sellmair.okay

fun interface OkAsync<T> {
    suspend fun await(): T
}
