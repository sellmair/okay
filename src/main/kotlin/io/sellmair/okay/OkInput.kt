package io.sellmair.okay

import java.nio.file.Path

sealed class OkInput

data class OkFileInput(val path: Path) : OkInput()

data class OkStringInput(val value: String) : OkInput()

data class OkCompositeInput(val values: List<OkInput>) : OkInput()

operator fun OkInput.plus(other: OkInput): OkCompositeInput {
    if (this is OkCompositeInput && other is OkCompositeInput) {
        return OkCompositeInput(this.values + other.values)
    }

    if (this is OkCompositeInput) {
        return OkCompositeInput(this.values + other)
    }

    if (other is OkCompositeInput) {
        return OkCompositeInput(listOf(this) + other.values)
    }

    return OkCompositeInput(listOf(this, other))
}