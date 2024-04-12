package io.sellmair.okay.input

import io.sellmair.okay.*
import kotlinx.serialization.Serializable

@Serializable
data class OkInputProperty(val name: String, val value: String) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash = hash {
        push(name)
        push(value)
    }
}
