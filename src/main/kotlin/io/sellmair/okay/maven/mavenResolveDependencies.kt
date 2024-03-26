package io.sellmair.okay.maven

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

fun OkContext.mavenResolveDependencies(): OkAsync<List<OkPath>> {
    val configurationFile = modulePath("okay.libs").system()
    val mavenLibrariesDirectory = path(".okay/libs/maven").system()
    return cachedTask(
        describeTask("mavenResolveDependencies"),
        input = OkFileInput(configurationFile),
        output = OkOutputDirectory(mavenLibrariesDirectory)
    ) {
        if (!configurationFile.isRegularFile()) return@cachedTask emptyList()

        val parsedCoordinates = configurationFile.readText().lines()
            .mapNotNull { line -> parseMavenCoordinates(line) }

        val resolvedDependencies = parsedCoordinates.map { coordinates ->
            mavenResolveDependency(mavenLibrariesDirectory, coordinates)
        }.map { it.await() }

        resolvedDependencies
    }
}

private val mavenCoordinatesRegex = Regex("""(.*):(.*):(.*)""")

private fun parseMavenCoordinates(value: String): MavenCoordinates? {
    val match = mavenCoordinatesRegex.matchEntire(value) ?: return null
    return MavenCoordinates(
        match.groups[1]?.value ?: return null,
        match.groups[2]?.value ?: return null,
        match.groups[3]?.value ?: return null,
    )
}
