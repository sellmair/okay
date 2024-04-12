package io.sellmair.okay

import io.sellmair.okay.input.OkInput
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.output.OkOutput
import io.sellmair.okay.serialization.Base64Serializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class OkCacheRecord(
    val session: OkSessionId,
    val descriptor: OkCoroutineDescriptor<@Contextual Any?>,
    val input: OkInput,
    val inputHash: OkHash,
    val dependencies: Set<OkHash>,

    val output: OkOutput? = null,
    val outputHash: OkHash? = null,
    @Serializable(Base64Serializer::class)
    val payload: ByteArray? = null,
    /**
     * Represents the state of all regular files associated with the cache record.
     * key: The path to the captured file
     * value: A hash of the file content (the file content can be retrieved from the cache using this hash)
     */
    val outputFiles: Map<OkPath, OkHash>? = null

)


