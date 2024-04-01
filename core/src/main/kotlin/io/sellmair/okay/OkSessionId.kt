package io.sellmair.okay

import java.io.Serializable
import java.util.UUID

class OkSessionId private constructor(private val value: UUID) : Serializable {
    companion object {
        val current = OkSessionId(UUID.randomUUID())
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
