package io.sellmair.okay.input

import io.sellmair.okay.OkState

interface OkInput : OkState {
    companion object {
        fun none(): OkInput = OkInputs(emptyList())
    }
}

