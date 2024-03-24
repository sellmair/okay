package io.sellmair.okay

import io.sellmair.okay.serialization.OkSerializable
import java.nio.file.Path

class OkCacheEntry<T>(
    val value: T,
    val outputHash: OkHash,
    val files: Map<Path, OkHash>,
) : OkSerializable