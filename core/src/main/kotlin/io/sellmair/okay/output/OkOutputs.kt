package io.sellmair.okay.output

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.hash
import kotlinx.serialization.Serializable

fun OkOutput(vararg output: OkOutput): OkOutput {
    val outputs = output.toList()
    return OkOutputs(outputs)
}

@Serializable
data class OkOutputs(val values: List<OkOutput>) : OkOutput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash {
            values.forEach { output ->
                push(output.currentHash(ctx))
            }
        }
    }
}

operator fun OkOutput.plus(other: OkOutput): OkOutput {
    if (this is OkOutputs && other is OkOutputs) {
        return OkOutputs(this.values + other.values)
    }

    if (this is OkOutputs) {
        return OkOutputs(this.values + other)
    }

    if (other is OkOutputs) {
        return OkOutputs(listOf(this) + other.values)
    }

    return OkOutput(this, other)
}