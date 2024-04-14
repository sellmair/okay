package io.sellmair.okay.fs

import okio.*
import okio.Path.Companion.toPath

fun OkFs(root: String): OkFs {
    return OkFsImpl(root.toPath())
}

sealed interface OkFs {

    fun path(path: String): OkPath = OkPath(path.toPath(), this)

    fun exists(path: OkPath): Boolean

    fun isRegularFile(path: OkPath): Boolean

    fun isDirectory(path: OkPath): Boolean

    fun absolutePath(path: OkPath): String

    fun createDirectories(path: OkPath)

    fun delete(path: OkPath)

    fun deleteRecursively(path: OkPath)

    fun write(path: OkPath, data: ByteArray)

    fun list(path: OkPath): List<OkPath>

    fun listOrNull(path: OkPath): List<OkPath>?

    fun listRecursively(path: OkPath): Sequence<OkPath>

    fun copy(from: OkPath, to: OkPath)

    fun source(path: OkPath): BufferedSource

    fun sink(path: OkPath): BufferedSink

    fun setIsExecutable(path: OkPath, isExecutable: Boolean)
}

internal val OkFs.impl
    get() = when (this) {
        is OkFsImpl -> this
    }

internal class OkFsImpl(
    val root: Path, private val fs: FileSystem = FileSystem.SYSTEM
) : OkFs {

    private val OkPath.resolved: Path
        get() = root.resolve(relative)

    override fun exists(path: OkPath): Boolean {
        return fs.exists(path.resolved)
    }

    override fun isRegularFile(path: OkPath): Boolean {
        return fs.metadataOrNull(path.resolved)?.isRegularFile == true
    }

    override fun isDirectory(path: OkPath): Boolean {
        return fs.metadataOrNull(path.resolved)?.isDirectory == true
    }

    override fun absolutePath(path: OkPath): String {
        return fs.canonicalize(".".toPath()).resolve(path.resolved).normalized().toString()
    }

    override fun createDirectories(path: OkPath) {
        fs.createDirectories(path.resolved, mustCreate = false)
    }

    override fun delete(path: OkPath) {
        fs.delete(path.resolved, mustExist = false)
    }

    override fun deleteRecursively(path: OkPath) {
        fs.deleteRecursively(path.resolved, mustExist = false)
    }

    override fun write(path: OkPath, data: ByteArray) {
        fs.write(path.resolved, mustCreate = false) {
            this.write(data)
        }
    }

    override fun list(path: OkPath): List<OkPath> {
        return fs.list(path.resolved).map { child -> OkPath(child.relativeTo(root), this) }
    }

    override fun listOrNull(path: OkPath): List<OkPath>? {
        return fs.listOrNull(path.resolved)?.map { child -> OkPath(child.relativeTo(root), this) }
    }

    override fun listRecursively(path: OkPath): Sequence<OkPath> {
        return fs.listRecursively(path.resolved, true).map { child ->
            // https://github.com/square/okio/issues/1468
            if (root == ".".toPath()) OkPath(child, this)
            else OkPath(child.relativeTo(root), this)
        }.emptyOnError()
    }

    override fun copy(from: OkPath, to: OkPath) {
        require(from.fs == to.fs) { "Expected paths fo of the same file system " }
        fs.copy(from.resolved, to.resolved)
    }

    override fun source(path: OkPath): BufferedSource {
        return fs.source(path.resolved).buffer()
    }

    override fun sink(path: OkPath): BufferedSink {
        return fs.sink(path.resolved, false).buffer()
    }

    override fun setIsExecutable(path: OkPath, isExecutable: Boolean) {
        return fs.setIsExecutable(path.resolved, isExecutable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OkFsImpl) return false
        if (this.root != other.root) return false
        if (fs::class != other.fs::class) return false
        return true
    }

    override fun hashCode(): Int {
        var hash = root.hashCode()
        hash = 31 * hash + fs::class.hashCode()
        return hash
    }
}

expect fun FileSystem.setIsExecutable(path: Path, isExecutable: Boolean)

private fun <T> Sequence<T>.emptyOnError() = sequence<T> {
    try {
        yieldAll(this@emptyOnError)
    } catch (t: Throwable) {
        // sorry folks.
    }
}