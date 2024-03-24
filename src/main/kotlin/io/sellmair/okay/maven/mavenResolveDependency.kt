package io.sellmair.okay.maven

import io.sellmair.okay.*
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

fun OkBuildContext.mavenResolveDependency(
    group: String, artifact: String, version: String
): OkAsync<Path> {
    val outputFile = Path("in/libs/$group-$artifact-$version.jar")

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
        val resultPath = Path("in/libs/$group-$artifact-$version.jar")
        resultPath.createParentDirectories()

        val urlString = buildString {
            append("https://repo1.maven.org/maven2/") // repo
            append(group.replace(".", "/"))
            append("/$artifact")
            append("/$version")
            append("/$artifact-$version.jar")
        }

        URI.create(urlString).toURL().openStream()
            .copyTo(resultPath.outputStream())

        outputFile
    }
}