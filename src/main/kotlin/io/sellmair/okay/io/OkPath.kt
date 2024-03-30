package io.sellmair.okay.io

import io.sellmair.okay.OkUnsafe
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.Path


class OkPath @OkUnsafe constructor(
    private val root: String,
    @property:OkUnsafe
    internal val value: String
) : Serializable {

    @OptIn(OkUnsafe::class)
    override fun toString(): String {
        return value
    }

    @OptIn(OkUnsafe::class)
    fun resolve(other: String): OkPath {
        return OkPath(root, Path(value).resolve(other).toString())
    }

    @OptIn(OkUnsafe::class)
    fun resolve(other: OkPath): OkPath {
        require(other.root == this.root)
        return OkPath(root, Path(value).resolve(other.value).toString())
    }

    @OptIn(OkUnsafe::class)
    fun system(): Path {
        return Path(root).resolve(value)
    }

    @OptIn(OkUnsafe::class)
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is OkPath) return false
        if (other.root != this.root) return false
        if (other.value != this.value) return false
        return true
    }

    @OptIn(OkUnsafe::class)
    override fun hashCode(): Int {
        var result = root.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

}

