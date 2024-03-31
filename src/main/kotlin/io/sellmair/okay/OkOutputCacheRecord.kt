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
    val output: OkOutput,
    val outputHash: OkHash,
    val outputState: Map<OkPath, OkHash>,
) : Serializable, OkInputCacheRecord


data class OkInputCacheRecordImpl(
    override val key: OkHash,
    override val input: OkInput,
    override val descriptor: OkCoroutineDescriptor<*>,
    override val dependencies: Set<OkHash>
) : OkInputCacheRecord, Serializable