package io.sellmair.okay

import java.io.Serializable
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun OkHash(hash: ByteArray): OkHash {
    return OkHash(Base64.UrlSafe.encode(hash))
}

data class OkHash(val value: String) : Serializable {

    override fun toString(): String {
        return value.take(6)
    }
}

interface HashBuilder {
    fun push(value: String)
    fun push(value: ByteArray)
    fun push(value: ByteArray, offset: Int, length: Int)
    fun push(value: Byte)
    fun push(value: OkHash)
}

fun hash(value: ByteArray): OkHash {
    val sha265 = MessageDigest.getInstance("SHA-256")
    sha265.update(value)
    return OkHash(sha265.digest())
}

fun hash(value: String): OkHash {
    return hash(value.encodeToByteArray())
}

fun hash(hashes: List<OkHash>): OkHash {
    return hash(hashes.joinToString(";") { it.value })
}

fun hash(builder: HashBuilder.() -> Unit): OkHash {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    return HashBuilderImpl(messageDigest).also(builder).build()
}

private class HashBuilderImpl(
    private val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
) : HashBuilder {

    override fun push(value: String) {
        messageDigest.update(value.encodeToByteArray())
    }

    override fun push(value: ByteArray) {
        messageDigest.update(value)
    }

    override fun push(value: ByteArray, offset: Int, length: Int) {
        messageDigest.update(value, offset, length)
    }

    override fun push(value: Byte) {
        messageDigest.update(value)
    }

    override fun push(value: OkHash) {
        push(value.value)
    }

    fun build(): OkHash {
        return OkHash(messageDigest.digest())
    }
}
