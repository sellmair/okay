package io.sellmair.okay.input

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.hash

data class OkInputs(val values: List<OkInput>) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash {
            values.forEach { value ->
                push(value.currentHash(ctx))
            }
        }
    }
}

fun OkInput(vararg inputs: OkInput) = OkInputs(inputs.toList())

fun Iterable<OkInput>.asInput(): OkInput = OkInputs(toList())

operator fun OkInput.plus(other: OkInput): OkInputs {
    if (this is OkInputs && other is OkInputs) {
        return OkInputs(this.values + other.values)
    }

    if (this is OkInputs) {
        return OkInputs(this.values + other)
    }

    if (other is OkInputs) {
        return OkInputs(listOf(this) + other.values)
    }

    return OkInputs(listOf(this, other))
}