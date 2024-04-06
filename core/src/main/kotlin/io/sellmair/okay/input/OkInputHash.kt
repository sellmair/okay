package io.sellmair.okay.input

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash


fun OkHash.asInput(): OkInput = OkInputHash(this)

private data class OkInputHash(val hash: OkHash) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash
    }
}