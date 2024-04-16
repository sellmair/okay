package io.sellmair.okay.output

import io.sellmair.okay.OkState

@kotlinx.serialization.Serializable
sealed interface OkOutput : OkState {
    companion object {
        fun none(): OkOutput = OkOutputs(emptyList())
    }
}
