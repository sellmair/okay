package io.sellmair.okay.serialization

import java.io.Serializable

interface OkSerializable : Serializable {
    fun writeReplace(): Any {
        return writeReplace(this)
    }
}