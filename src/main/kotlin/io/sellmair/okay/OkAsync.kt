package io.sellmair.okay



interface OkAsync<T> {
    suspend fun await(ctx: OkContext): T = ctx.await0(this)
}

fun <T, R> OkAsync<T>.map(mapper: suspend (T) -> R): OkAsync<R> = object : OkAsync<R> {
    override suspend fun await(ctx: OkContext): R {
        val source = this@map
        val value = source.await(ctx)
        return mapper(value)
    }
}

fun <T, R> OkAsync<T>.flatMap(mapper: suspend (T) -> OkAsync<R>): OkAsync<R> = object : OkAsync<R> {
    override suspend fun await(ctx: OkContext): R {
        val source = this@flatMap
        val value = source.await(ctx)
        return mapper(value).await(ctx)
    }
}
