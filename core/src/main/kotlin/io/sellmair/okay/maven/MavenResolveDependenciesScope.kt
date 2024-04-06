package io.sellmair.okay.maven

internal enum class MavenResolveDependenciesScope {
    Compile, Runtime
}

internal operator fun MavenResolveDependenciesScope.contains(dependency: MavenPom.MavenDependency): Boolean {
    return when (this) {
        MavenResolveDependenciesScope.Compile -> dependency.scope == MavenPom.MavenDependency.Scope.Compile ||
                dependency.scope == MavenPom.MavenDependency.Scope.Provided

        MavenResolveDependenciesScope.Runtime -> dependency.scope == MavenPom.MavenDependency.Scope.Runtime ||
                dependency.scope == MavenPom.MavenDependency.Scope.Compile
    }
}