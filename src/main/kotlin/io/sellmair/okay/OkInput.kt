package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import java.io.Serializable
import kotlin.io.path.isDirectory

abstract class OkInput : Serializable {
    abstract suspend fun cacheKey(ctx: OkContext): OkHash

    companion object {
        fun none(): OkInput = OkInputs(emptyList())
    }
}

fun OkInput(vararg inputs: OkInput) = OkInputs(inputs.toList())

data class OkInputFile(val path: OkPath) : OkInput() {
    override suspend fun cacheKey(ctx: OkContext): OkHash {
        val systemPath = path.system()
        return if (systemPath.isDirectory()) systemPath.directoryCacheKey()
        else systemPath.regularFileCacheKey()
    }
}

fun OkInputString(value: String) = OkHashInput(hash(value))

data class OkHashInput(val hash: OkHash) : OkInput() {
    override suspend fun cacheKey(ctx: OkContext): OkHash {
        return hash
    }
}

data class OkInputs(val values: List<OkInput>) : OkInput() {
    override suspend fun cacheKey(ctx: OkContext): OkHash {
        return hash {
            values.forEach { value ->
                push(value.cacheKey(ctx))
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