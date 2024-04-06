package io.sellmair.okay

import io.sellmair.okay.input.OkInput
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.output.OkOutput
import java.io.Serializable

data class OkCacheRecord<T>(
    val session: OkSessionId,
    val descriptor: OkCoroutineDescriptor<T>,
    val input: OkInput,
    val inputHash: OkHash,
    val dependencies: Set<OkHash>,

    val output: OkOutput? = null,
    val outputHash: OkHash? = null,
    val outputValue: T? = null,
    /**
     * Represents the state of all regular files associated with the cache record.
     * key: The path to the captured file
     * value: A hash of the file content (the file content can be retrieved from the cache using this hash)
     */
    val outputFiles: Map<OkPath, OkHash>? = null

) : Serializable


