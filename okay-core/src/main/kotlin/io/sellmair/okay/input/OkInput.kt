package io.sellmair.okay.input

import io.sellmair.okay.OkState
import java.io.Serializable

interface OkInput : Serializable, OkState {
    companion object {
        fun none(): OkInput = OkInputs(emptyList())
    }
}

