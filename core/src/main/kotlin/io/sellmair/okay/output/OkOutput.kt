package io.sellmair.okay.output

import io.sellmair.okay.OkState
import kotlinx.serialization.Serializer
import java.io.Serializable

@kotlinx.serialization.Serializable
sealed interface OkOutput : OkState, Serializable {
    companion object {
        fun none(): OkOutput = OkOutputs(emptyList())
    }
}
