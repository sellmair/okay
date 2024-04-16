@file:OptIn(ExperimentalSerializationApi::class)

package io.sellmair.okay.serialization

import io.sellmair.okay.OkCoroutineDescriptor
import io.sellmair.okay.input.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val module = SerializersModule {
    polymorphic(OkInput::class) {
        subclass(OkInputs.serializer())
        subclass(OkInputFile.serializer())
        subclass(OkInputProperty.serializer())
        subclass(OkInputProperty.serializer())
        subclass(OkInputString.serializer())
        subclass(OkInputHash.serializer())
        subclass(OkInputFileCollection.serializer())
        subclass(OkCoroutineDescriptor.serializer(NothingSerializer()))
    }
}
