package io.sellmair.okay.fs

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

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
    
}

internal val OkFs.impl get() = when(this) {
    is OkFsImpl -> this
}

internal class OkFsImpl(
    val root: Path, private val delegate: FileSystem = FileSystem.SYSTEM
): OkFs {
    
    private val OkPath.resolved: Path
        get() = root.resolve(relative)
        
    override fun exists(path: OkPath): Boolean {
        return delegate.exists(path.resolved)
    }

    override fun isRegularFile(path: OkPath): Boolean {
        return delegate.metadataOrNull(path.resolved)?.isRegularFile == true
    }

    override fun isDirectory(path: OkPath): Boolean {
        return delegate.metadataOrNull(path.resolved)?.isDirectory == true
    }

    override fun absolutePath(path: OkPath): String {
        return path.resolved.toString()
    }

    override fun createDirectories(path: OkPath) {
        delegate.createDirectories(path.resolved, mustCreate = true)
    }

    override fun delete(path: OkPath) {
        delegate.delete(path.resolved, mustExist = false)
    }

    override fun deleteRecursively(path: OkPath) {
        delegate.deleteRecursively(path.resolved, mustExist = false)
    }

    override fun write(path: OkPath, data: ByteArray) {
        delegate.write(path.resolved, mustCreate = false) {
            this.write(data)
        }
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is OkFsImpl) return false
        if(this.root != other.root) return false
        if(delegate::class != other.delegate::class) return false
        return true
    }

    override fun hashCode(): Int {
        var hash = root.hashCode()
        hash = 31 * hash + delegate::class.hashCode()
        return hash
    }
}
