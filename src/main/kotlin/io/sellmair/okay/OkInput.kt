package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import java.io.Serializable
import java.nio.file.Path

sealed class OkInput: Serializable

data class OkFileInput(val path: OkPath) : OkInput() {
    constructor(path: Path) : this(path.toOk())
}

data class OkStringInput(val value: String) : OkInput()

data class OkCompositeInput(val values: List<OkInput>) : OkInput()

fun OkInput(vararg inputs: OkInput) = OkCompositeInput(inputs.toList())

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