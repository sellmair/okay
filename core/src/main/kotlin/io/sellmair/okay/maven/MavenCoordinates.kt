package io.sellmair.okay.maven

import java.io.Serializable

@kotlinx.serialization.Serializable
data class MavenCoordinates(val group: String, val artifact: String, val version: String): Serializable {
    override fun toString(): String {
        return "$group:$artifact:$version"
    }
}