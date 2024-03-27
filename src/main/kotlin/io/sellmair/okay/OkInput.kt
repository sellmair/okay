package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.isDirectory

abstract class OkInput : Serializable {
    fun cacheKey(): OkHash = currentState()
    abstract fun currentState(): OkHash

    companion object {
        fun none(): OkInput = OkCompositeInput(emptyList())
    }
}

fun OkInput(vararg inputs: OkInput) = OkCompositeInput(inputs.toList())

data class OkFileInput(val path: OkPath) : OkInput() {
    constructor(path: Path) : this(path.toOk())

    override fun currentState(): OkHash {
        val systemPath = path.system()
        return if (systemPath.isDirectory()) systemPath.directoryCacheKey()
        else systemPath.regularFileCacheKey()
    }
}

data class OkStringInput(val value: String) : OkInput() {
    override fun currentState(): OkHash {
        return hash(value)
    }
}

data class OkCompositeInput(val values: List<OkInput>) : OkInput() {
    override fun currentState(): OkHash {
        return hash {
            values.forEach { value ->
                push(value.currentState())
            }
        }
    }
}


operator fun OkInput.plus(other: OkInput): OkCompositeInput {
    if (this is OkCompositeInput && other is OkCompositeInput) {
        return OkCompositeInput(this.values + other.values)
    }

    if (this is OkCompositeInput) {
        return OkCompositeInput(this.values + other)
    }

    if (other is OkCompositeInput) {
        return OkCompositeInput(listOf(this) + other.values)
    }

    return OkCompositeInput(listOf(this, other))
}