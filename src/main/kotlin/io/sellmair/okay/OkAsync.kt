package io.sellmair.okay


interface OkAsync<T> {
    suspend fun await(): T
}
