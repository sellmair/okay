package io.sellmair.okay.input

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import kotlinx.serialization.Serializable


fun OkHash.asInput(): OkInput = OkInputHash(this)

@Serializable
internal data class OkInputHash(val hash: OkHash) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash
    }
}