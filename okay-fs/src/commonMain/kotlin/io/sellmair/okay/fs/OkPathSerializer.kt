package io.sellmair.okay.fs

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import okio.Path.Companion.toPath


internal object OkPathSerializer : KSerializer<OkPath> {
    override val descriptor = buildClassSerialDescriptor("OkPath") {
        element<String>("root")
        element<String>("path")
    }
    
    override fun deserialize(decoder: Decoder): OkPath {
        return decoder.decodeStructure(descriptor) {
            var root: String? = null
            var path: String? = null
            
            while(true) {
                when(val index = decodeElementIndex(descriptor)) {
                    0 -> root = decodeStringElement(descriptor, index)
                    1 -> path = decodeStringElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                }
            }
            
             OkPath(
                relative = (path ?: error("Missing 'path'")).toPath(),
                fs = OkFsImpl((root ?: error("Missing 'root'")).toPath())
            )
        }
    }

    override fun serialize(encoder: Encoder, value: OkPath) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.fs.impl.root.toString())
            encodeStringElement(descriptor, 1, value.relative.toString())
        }
    }
}
