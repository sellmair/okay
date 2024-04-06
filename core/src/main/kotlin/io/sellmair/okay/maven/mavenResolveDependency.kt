package io.sellmair.okay.maven

import io.sellmair.okay.OkContext
import io.sellmair.okay.output.OkOutputFile
import io.sellmair.okay.cachedCoroutine
import io.sellmair.okay.describeRootCoroutine
import io.sellmair.okay.input.OkInput
import io.sellmair.okay.input.OkInputString
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.log
import java.net.URI
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

suspend fun OkContext.mavenResolveDependency(
    outputDirectory: OkPath,
    mavenCoordinates: MavenCoordinates,
): OkPath {
    val group = mavenCoordinates.group
    val artifact = mavenCoordinates.artifact
    val version = mavenCoordinates.version

    val outputFile = outputDirectory.resolve("$group-$artifact-$version.jar")

    return cachedCoroutine(
        describeRootCoroutine("resolve: '$group:$artifact:$version'"),
        input = OkInput(
            OkInputString(group),
            OkInputString(artifact),
            OkInputString(version)
        ),
        output = OkOutputFile(outputFile)
    ) {
        log("Downloading '$ansiGreen$mavenCoordinates$ansiReset'")

        outputFile.system().createParentDirectories()

        val urlString = buildString {
            append("https://repo1.maven.org/maven2/") // repo
            append(group.replace(".", "/"))
            append("/$artifact")
            append("/$version")
            append("/$artifact-$version.jar")
        }

        mavenResolvePom(mavenCoordinates)

        URI.create(urlString).toURL().openStream().use { inputStream ->
            outputFile.system().outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        outputFile
    }
}