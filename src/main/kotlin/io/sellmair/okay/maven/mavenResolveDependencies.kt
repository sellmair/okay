package io.sellmair.okay.maven

import io.sellmair.okay.*
import io.sellmair.okay.OkTaskDescriptor.Verbosity.Info
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.dependency.runtimeDependenciesClosure

private enum class MavenResolveDependenciesScope {
    Compile, Runtime
}

fun OkContext.mavenResolveCompileDependencies(): OkAsync<List<OkPath>> {
    return mavenResolveRuntimeDependencies(MavenResolveDependenciesScope.Compile)
}

fun OkContext.mavenResolveRuntimeDependencies(): OkAsync<List<OkPath>> {
    return mavenResolveRuntimeDependencies(MavenResolveDependenciesScope.Runtime)
}

private fun OkContext.mavenResolveRuntimeDependencies(scope: MavenResolveDependenciesScope): OkAsync<List<OkPath>> {
    val mavenLibrariesDirectory = path(".okay/libs/maven").system()
    return launchCachedCoroutine(
        describeTask("mavenResolve${scope}Dependencies", verbosity = Info),
        input = OkInput.none(),
        output = OkOutput.none()
    ) {
        val parsedCoordinates = dependenciesClosure(scope).await()
            .mapNotNull { declaration -> parseMavenCoordinates(declaration.value) }

        val resolvedDependencies = parsedCoordinates.map { coordinates ->
            mavenResolveDependency(mavenLibrariesDirectory, coordinates)
        }.map { it.await() }

        resolvedDependencies
    }
}

private fun OkContext.dependenciesClosure(scope: MavenResolveDependenciesScope) = when (scope) {
    MavenResolveDependenciesScope.Compile -> compileDependenciesClosure()
    MavenResolveDependenciesScope.Runtime -> runtimeDependenciesClosure()
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
