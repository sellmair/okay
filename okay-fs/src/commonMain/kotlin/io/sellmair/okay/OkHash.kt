package io.sellmair.okay

import io.sellmair.okay.fs.OkPath
import okio.HashingSink.Companion.sha256
import okio.Source
import okio.blackholeSink
import okio.buffer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun OkHash(hash: ByteArray): OkHash {
    return OkHash(Base64.UrlSafe.encode(hash))
}

@kotlinx.serialization.Serializable
data class OkHash(val value: String) {
    override fun toString(): String {
        return value.take(6)
    }
}


fun HashBuilder(): HashBuilder = HashBuilderImpl()

interface HashBuilder {
    fun push(value: String)

    fun push(value: ByteArray)

    fun push(value: ByteArray, offset: Int, length: Int)

    fun push(value: Boolean)

    fun push(value: Byte)

    fun push(value: OkHash)

    fun push(value: OkPath)

    fun push(value: Source)

    fun build(): OkHash
}

fun hash(value: ByteArray): OkHash {
    return hash { push(value) }
}

fun hash(value: String): OkHash {
    return hash(value.encodeToByteArray())
}

fun hash(hashes: List<OkHash>): OkHash {
    return hash(hashes.joinToString(";") { it.value })
}

inline fun hash(builder: HashBuilder.() -> Unit): OkHash {
    return HashBuilder().also(builder).build()
}

fun Iterable<OkHash>.hash(): OkHash = hash {
    forEach { value -> push(value) }
}

class HashBuilderImpl : HashBuilder {

    private val hashingSink = sha256(blackholeSink())

    private val buffer = sha256(hashingSink).buffer()

    override fun push(value: String) {
        buffer.writeUtf8(value)
    }

    override fun push(value: ByteArray) {
        buffer.write(value)
    }

    override fun push(value: ByteArray, offset: Int, length: Int) {
        buffer.write(value, offset, length)
    }

    override fun push(value: Boolean) {
        buffer.writeByte(if (value) 1 else 0)
    }

    override fun push(value: Byte) {
        buffer.writeByte(value.toInt())
    }

    override fun push(value: OkHash) {
        buffer.writeUtf8(value.value)
    }

    override fun push(value: OkPath) {
        push(value.toString())
    }

    override fun push(value: Source) {
        try {
            buffer.writeAll(value)
        } finally {
            value.close()
        }
    }

    override fun build(): OkHash {
        buffer.close()
        return OkHash(hashingSink.hash.base64Url())
    }
}
