package io.sellmair.okay.input

import io.sellmair.okay.OkCoroutineDescriptor
import io.sellmair.okay.OkState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.Serializable

interface OkInput : Serializable, OkState {
    companion object {
        fun none(): OkInput = OkInputs(emptyList())
    }
}

