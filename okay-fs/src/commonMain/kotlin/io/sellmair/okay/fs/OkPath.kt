package io.sellmair.okay.fs

import kotlinx.serialization.Serializable
import okio.BufferedSink
import okio.BufferedSource
import okio.Path
import okio.use

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

    val extension: String
        get() = relative.segments.lastOrNull()
            ?.split(".")?.lastOrNull().orEmpty()

    val name: String
        get() = relative.name

    fun resolve(child: String): OkPath = OkPath(relative.resolve(child), fs)

    fun relativeTo(other: OkPath): String {
        return relative.relativeTo(other.relative).toString()
    }

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

fun OkPath.createParentDirectories() {
    parent?.createDirectories()
}

fun OkPath.delete() = fs.delete(this)

fun OkPath.deleteRecursively() = fs.deleteRecursively(this)

fun OkPath.write(data: ByteArray) = fs.write(this, data)

fun OkPath.writeText(value: String) = sink().use { sink ->
    sink.writeUtf8(value)
}

fun OkPath.readAll(): ByteArray {
    return source().use { it.readByteArray() }
}

fun OkPath.readText(): String {
    return source().use { it.readUtf8() }
}

fun OkPath.list(): List<OkPath> = fs.list(this)

fun OkPath.listOrNull(): List<OkPath>? = fs.listOrNull(this)

fun OkPath.listRecursively(): Sequence<OkPath> = fs.listRecursively(this)

fun OkPath.absolutePathString(): String = fs.absolutePath(this)

fun OkPath.setIsExecutable(isExecutable: Boolean) = fs.setIsExecutable(this, isExecutable)

fun OkPath.copyTo(other: OkPath): OkPath {
    fs.copy(this, other)
    return other
}

fun OkPath.source(): BufferedSource {
    return fs.source(this)
}

fun OkPath.source(use: (sink: BufferedSource) -> Unit) {
    source().use(use)
}

fun OkPath.sink(): BufferedSink {
    return fs.sink(this)
}

fun OkPath.sink(use: (sink: BufferedSink) -> Unit) {
    sink().use(use)
}