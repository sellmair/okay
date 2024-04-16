package io.sellmair.okay

interface OkState {
    suspend fun currentHash(ctx: OkContext): OkHash
}
