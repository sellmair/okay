package io.sellmair.okay.fs

import kotlinx.serialization.Serializable
import okio.Path

@Serializable(OkPathSerializer::class)
class OkPath internal constructor(
    internal val relative: Path,
    internal val fs: OkFs
) {
    init {
        require(relative.isRelative) { "Expected '$relative' to be a relative path" }
    }

    val parent: OkPath?
        get() = relative.parent?.let { parentPath -> OkPath(parentPath, fs) }

    override fun toString(): String {
        return relative.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OkPath) return false
        if (this.relative != other.relative) return false
        if (this.fs != other.fs) return false
        return true
    }

    override fun hashCode(): Int {
        var hash = relative.hashCode()
        hash = hash * 31 + fs.hashCode()
        return hash
    }
}

fun OkPath.isRegularFile(): Boolean = fs.isRegularFile(this)

fun OkPath.isDirectory(): Boolean = fs.isDirectory(this)

fun OkPath.createDirectories() = fs.createDirectories(this)

fun OkPath.delete() = fs.delete(this)

fun OkPath.write(data: ByteArray) = fs.write(this, data)