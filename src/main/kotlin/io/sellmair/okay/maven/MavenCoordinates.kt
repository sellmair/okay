package io.sellmair.okay.maven

data class MavenCoordinates(val group: String, val artifact: String, val version: String) {
    override fun toString(): String {
        return "$group:$artifact:$version"
    }
}