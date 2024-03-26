package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import java.io.Serializable

class OkCacheEntry<T>(
    val key: OkHash,
    val value: T,
    val taskDescriptor: OkTaskDescriptor<T>,
    val input: OkInput,
    val output: OkOutput,
    val outputHash: OkHash,
    val dependencies: List<OkHash>,
    val files: Map<OkPath, OkHash>,
) : Serializable