package io.sellmair.okay


import io.ktor.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@kotlinx.serialization.Serializable
class OkSessionId private constructor(private val value: String) : CoroutineContext.Element {
    override val key get() = Key

    companion object Key : CoroutineContext.Key<OkSessionId> {
        fun random() = OkSessionId(Random.nextBytes(32).encodeBase64())
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is OkSessionId) return false
        return other.value == this.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

fun OkContext.currentOkSessionId() = cs.coroutineContext.currentOkSessionId()

fun CoroutineContext.currentOkSessionId(): OkSessionId {
    return get(OkSessionId) ?: error("Missing 'OkSessionId' ")
}