@file:OptIn(ExperimentalSerializationApi::class)

package io.sellmair.okay.serialization

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

internal val format: BinaryFormat = ProtoBuf {
    serializersModule = module
}
