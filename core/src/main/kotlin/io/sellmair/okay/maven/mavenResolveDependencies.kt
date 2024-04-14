package io.sellmair.okay.maven

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.input.OkInput
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.output.OkOutput


suspend fun OkContext.mavenResolveCompileDependencies(): List<OkPath> {
    return mavenResolveRuntimeDependencies(MavenResolveDependenciesScope.Compile)
}

suspend fun OkContext.mavenResolveRuntimeDependencies(): List<OkPath> {
    return mavenResolveRuntimeDependencies(MavenResolveDependenciesScope.Runtime)
}

private suspend fun OkContext.mavenResolveRuntimeDependencies(scope: MavenResolveDependenciesScope): List<OkPath> {
    val mavenLibrariesDirectory = path(".okay/libs/maven")
    return cachedCoroutine(
        describeCoroutine("mavenResolve${scope}Dependencies", verbosity = Info),
        input = OkInput.none(),
        output = OkOutput.none()
    ) {
        val parsedCoordinates = dependenciesClosure(scope)
            .mapNotNull { declaration -> parseMavenCoordinates(declaration.value) }

        val allDependencies = mavenResolveDependencyTree(parsedCoordinates, scope)
            .resolveConflicts()

        val resolvedDependencies = allDependencies.map { coordinates ->
            async { mavenResolveDependency(mavenLibrariesDirectory, coordinates) }
        }.awaitAll()

        resolvedDependencies
    }
}

private suspend fun OkContext.dependenciesClosure(scope: MavenResolveDependenciesScope) = when (scope) {
    MavenResolveDependenciesScope.Compile -> compileDependenciesClosure()
    MavenResolveDependenciesScope.Runtime -> runtimeDependenciesClosure()
}

private fun Iterable<MavenCoordinates>.resolveConflicts(): List<MavenCoordinates> {
    val currentValues = mutableMapOf<String, MavenCoordinates>()
    forEach { coordinates ->
        val module = "${coordinates.group}:${coordinates.artifact}"
        val currentCandidate = currentValues[module]
        if (currentCandidate == null) currentValues[module] = coordinates
        else currentValues[module] = listOf(currentCandidate, coordinates).maxBy { it.version }
    }

    return currentValues.values.toList()
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
