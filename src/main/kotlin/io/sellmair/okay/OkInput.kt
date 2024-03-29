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
        fun none(): OkInput = OkInputs(emptyList())
    }
}

fun OkInput(vararg inputs: OkInput) = OkInputs(inputs.toList())

data class OkInputFile(val path: OkPath) : OkInput() {
    constructor(path: Path) : this(path.toOk())

    override fun currentState(): OkHash {
        val systemPath = path.system()
        return if (systemPath.isDirectory()) systemPath.directoryCacheKey()
        else systemPath.regularFileCacheKey()
    }
}

data class OkInputString(val value: String) : OkInput() {
    override fun currentState(): OkHash {
        return hash(value)
    }
}

data class OkInputs(val values: List<OkInput>) : OkInput() {
    override fun currentState(): OkHash {
        return hash {
            values.forEach { value ->
                push(value.currentState())
            }
        }
    }
}


operator fun OkInput.plus(other: OkInput): OkInputs {
    if (this is OkInputs && other is OkInputs) {
        return OkInputs(this.values + other.values)
    }

    if (this is OkInputs) {
        return OkInputs(this.values + other)
    }

    if (other is OkInputs) {
        return OkInputs(listOf(this) + other.values)
    }

    return OkInputs(listOf(this, other))
}