package io.sellmair.okay.maven

import io.sellmair.okay.OkAsync
import io.sellmair.okay.OkContext
import io.sellmair.okay.OkFileInput
import io.sellmair.okay.OkOutputDirectory
import io.sellmair.okay.io.OkLightPath
import io.sellmair.okay.io.okLight
import kotlin.io.path.Path
import kotlin.io.path.readText

fun OkContext.mavenResolveDependencies(): OkAsync<List<OkLightPath>> {
    val configurationFile = Path("okay")
    val mavenLibrariesDirectory = Path(".okay/libs/maven")
    return cached(
        "resolve maven dependencies",
        input = OkFileInput(configurationFile),
        output = OkOutputDirectory(mavenLibrariesDirectory)
    ) {
        val parsedCoordinates = configurationFile.readText().lines()
            .mapNotNull { line -> parseMavenCoordinates(line) }

        val resolvedDependencies = parsedCoordinates.map { coordinates ->
            mavenResolveDependency(mavenLibrariesDirectory, coordinates)
        }.map { it.await() }

        resolvedDependencies.map { path -> path.okLight() }
    }
}

private val mavenCoordinatesRegex = Regex("""m/(.*):(.*):(.*)""")

private fun parseMavenCoordinates(value: String): MavenCoordinates? {
    val match = mavenCoordinatesRegex.matchEntire(value) ?: return null
    return MavenCoordinates(
        match.groups[1]?.value ?: return null,
        match.groups[2]?.value ?: return null,
        match.groups[3]?.value ?: return null,
    )
}
