package io.sellmair.okay.input

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.hash
import kotlinx.serialization.Serializable

@Serializable
data class OkInputString(val value: String) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash(value)
    }
}

