package io.sellmair.okay.io

import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.Path

@JvmInline
value class OkPath(val path: String) : Serializable {

    override fun toString(): String {
        return path
    }

    fun resolve(other: String): OkPath {
        return Path(path).resolve(other).toOk()
    }

    fun resolve(other: OkPath): OkPath {
        return Path(path).resolve(other.path).toOk()
    }

    fun toPath() = Path(path)
    fun system(): Path = Path(path)
}

fun Path.toOk() = OkPath(toString())