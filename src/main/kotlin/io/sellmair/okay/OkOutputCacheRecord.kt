package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import java.io.Serializable

internal sealed interface OkInputCacheRecord {
    val key: OkHash
    val input: OkInput
    val descriptor: OkCoroutineDescriptor<*>
    val dependencies: Set<OkHash>
}

data class OkOutputCacheRecord<T>(
    override val key: OkHash,
    override val input: OkInput,
    override val descriptor: OkCoroutineDescriptor<T>,
    override val dependencies: Set<OkHash>,

    val value: T,

    /**
     * The output associated with the cache record
     * Can be used to check if the record is dirty (if recalculating the hash does not match the [outputHash])
     */
    val output: OkOutput,

    /**
     * The hash of the output
     */
    val outputHash: OkHash,

    /**
     * Represents the state of all regular files associated with the cache record.
     * key: The path to the captured file
     * value: A hash of the file content (the file content can be retrieved from the cache using this hash)
     */
    val outputSnapshot: Map<OkPath, OkHash>,
) : Serializable, OkInputCacheRecord


data class OkInputCacheRecordImpl(
    override val key: OkHash,
    override val input: OkInput,
    override val descriptor: OkCoroutineDescriptor<*>,
    override val dependencies: Set<OkHash>
) : OkInputCacheRecord, Serializable