package io.sellmair.okay

import java.io.Serializable
import java.util.UUID
import kotlin.coroutines.CoroutineContext

@kotlinx.serialization.Serializable
class OkSessionId private constructor(private val value: String) : Serializable, CoroutineContext.Element {
    override val key get() = Key

    companion object Key : CoroutineContext.Key<OkSessionId> {
        fun random() = OkSessionId(UUID.randomUUID().toString())
    }

    override fun toString(): String {
        return value.toString()
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