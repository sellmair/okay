package io.sellmair.okay.maven

import io.sellmair.okay.*
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

fun OkContext.mavenResolveDependency(
    outputDirectory: Path,
    mavenCoordinates: MavenCoordinates,
): OkAsync<Path> {
    val group = mavenCoordinates.group
    val artifact = mavenCoordinates.artifact
    val version = mavenCoordinates.version

    runBlocking {  }
    val outputFile = outputDirectory.resolve("$group-$artifact-$version.jar")

    return cached(
        "resolve: '$group:$artifact:$version'",
        input = listOf(
            OkStringInput(group),
            OkStringInput(artifact),
            OkStringInput(version)
        ),
        output = listOf(OkOutputFile(outputFile))
    ) {
        log("Downloading $artifact:$version")

        outputFile.createParentDirectories()

        val urlString = buildString {
            append("https://repo1.maven.org/maven2/") // repo
            append(group.replace(".", "/"))
            append("/$artifact")
            append("/$version")
            append("/$artifact-$version.jar")
        }

        URI.create(urlString).toURL().openStream()
            .copyTo(outputFile.outputStream())

        outputFile
    }
}