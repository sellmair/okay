package io.sellmair.okay.serialization

import io.sellmair.okay.OkCacheEntry
import io.sellmair.okay.OkHash
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString


private class PathSurrogate(val path: String) : Serializable {
    fun readResolve(): Any = Path(path)
}

private class OkCacheEntrySurrogate(
    private val value: Any?,
    private val outputHash: OkHash,
    private val files: Map<String, OkHash>
) : Serializable {
    private fun readResolve(): Any {
        return OkCacheEntry(
            value = value, outputHash = outputHash,
            files = files.mapKeys { Path(it.key) },
        )
    }
}

internal fun writeReplace(any: Any): Any {
    if (any is Path) return PathSurrogate(any.absolutePathString())
    if (any is OkCacheEntry<*>) return OkCacheEntrySurrogate(
        if (any.value != null) writeReplace(any.value) else null, any.outputHash,
        files = any.files.mapKeys { (key, _) -> key.absolutePathString() }
    )
    return any
}
