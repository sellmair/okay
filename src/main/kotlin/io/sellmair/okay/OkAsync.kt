package io.sellmair.okay


interface OkAsync<T> {
    suspend fun await(): T
}

fun <T> OkAsync(value: T): OkAsync<T> = object : OkAsync<T> {
    override suspend fun await(): T {
        return value
    }
}