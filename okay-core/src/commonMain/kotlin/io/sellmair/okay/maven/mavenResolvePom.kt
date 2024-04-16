package io.sellmair.okay.maven

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import io.sellmair.okay.OkContext
import io.sellmair.okay.cachedCoroutine
import io.sellmair.okay.describeRootCoroutine
import io.sellmair.okay.input.OkInputString
import io.sellmair.okay.output.OkOutput
import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.log
import kotlin.use

@kotlinx.serialization.Serializable
data class MavenPom(
    val packaging: String?,
    val dependencies: List<MavenDependency>
)  {

    @kotlinx.serialization.Serializable
    data class MavenDependency(
        val coordinates: MavenCoordinates,
        val scope: Scope
    )  {
        enum class Scope {
            Compile, Runtime, Provided;

            companion object {
                fun orNull(value: String?): Scope? {
                    if (value == null) return null
                    return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                }
            }
        }
    }
}

suspend fun OkContext.mavenResolvePom(coordinates: MavenCoordinates): MavenPom? {
    val group = coordinates.group
    val artifact = coordinates.artifact
    val version = coordinates.version

    return cachedCoroutine(
        describeRootCoroutine("mavenResolvePom"),
        input = OkInputString(coordinates.toString()),
        output = OkOutput.none()
    ) {

        log("Downloading POM '$ansiGreen$coordinates$ansiReset'")
        val urlString = buildString {
            append("https://repo1.maven.org/maven2/") // repo
            append(group.replace(".", "/"))
            append("/$artifact")
            append("/$version")
            append("/$artifact-$version.pom")
        }

        val pomModel = HttpClient().use { client ->
            client.prepareRequest(urlString).execute { response ->
                if (response.status == HttpStatusCode.NotFound) return@execute null
                Any()
            }
        } ?: return@cachedCoroutine null

        null

        /*
        MavenPom(
            packaging = pomModel.packaging,
            dependencies = pomModel.dependencies.mapNotNull { dependency ->
                MavenPom.MavenDependency(
                    coordinates = MavenCoordinates(
                        group = dependency.groupId,
                        artifact = dependency.artifactId,
                        version = san(dependency.version ?: return@mapNotNull null)
                    ),
                    scope = MavenPom.MavenDependency.Scope.orNull(dependency.scope) ?: return@mapNotNull null
                )
            }
        )

         */
    }
}

