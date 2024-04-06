package io.sellmair.okay.output

import io.sellmair.okay.OkState
import java.io.Serializable

sealed interface OkOutput : OkState, Serializable {
    companion object {
        fun none(): OkOutput = OkOutputs(emptyList())
    }
}

