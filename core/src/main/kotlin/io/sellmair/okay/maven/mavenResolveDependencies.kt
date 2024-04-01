package io.sellmair.okay.maven

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.utils.withClosure


private enum class MavenResolveDependenciesScope {
    Compile, Runtime
}

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
            .withClosure<MavenCoordinates> { declaration ->
                mavenResolvePom(declaration)?.dependencies.orEmpty().filter { it in scope }.map { it.coordinates }
            }
            .resolveConflicts()

        val resolvedDependencies = parsedCoordinates.map { coordinates ->
            async { mavenResolveDependency(mavenLibrariesDirectory, coordinates) }
        }.awaitAll()

        resolvedDependencies
    }
}

private suspend fun OkContext.dependenciesClosure(scope: MavenResolveDependenciesScope) = when (scope) {
    MavenResolveDependenciesScope.Compile -> compileDependenciesClosure()
    MavenResolveDependenciesScope.Runtime -> runtimeDependenciesClosure()
}

private operator fun MavenResolveDependenciesScope.contains(dependency: MavenPom.MavenDependency): Boolean {
    return when (this) {
        MavenResolveDependenciesScope.Compile -> dependency.scope == MavenPom.MavenDependency.Scope.Compile ||
                dependency.scope == MavenPom.MavenDependency.Scope.Provided

        MavenResolveDependenciesScope.Runtime -> dependency.scope == MavenPom.MavenDependency.Scope.Runtime ||
                dependency.scope == MavenPom.MavenDependency.Scope.Compile
    }
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
