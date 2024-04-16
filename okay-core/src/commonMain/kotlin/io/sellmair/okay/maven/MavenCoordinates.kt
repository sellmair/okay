package io.sellmair.okay.maven

import kotlinx.serialization.Serializable


@Serializable
data class MavenCoordinates(val group: String, val artifact: String, val version: String) {
    override fun toString(): String {
        return "$group:$artifact:$version"
    }
}