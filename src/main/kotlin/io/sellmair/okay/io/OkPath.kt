package io.sellmair.okay.io

import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.Path

@JvmInline
value class OkLightPath(val path: String) : Serializable {
    fun toPath() = Path(path)
}

fun Path.okLight() = OkLightPath(toString())