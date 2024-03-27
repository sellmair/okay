package io.sellmair.okay.dependency

import io.sellmair.okay.*
import io.sellmair.okay.utils.withClosure
import io.sellmair.okay.withModule

fun OkContext.compileDependenciesClosure(): OkAsync<Set<OkDependencyDeclaration>> {
    return launchMemoizedCoroutine(describeCoroutine("compileDependenciesClosure"), input = OkInput.none()) {
        parseDependenciesFile().await()?.declarations.orEmpty()
            .filter { it.isCompile }
            .withClosure<OkDependencyDeclaration> closure@{ declaration ->
                val module = declaration.dependencyModulePath() ?: return@closure emptyList()
                withModule(module) {
                    parseDependenciesFile().await()?.declarations.orEmpty().filter { it.isExported && it.isCompile }
                }
            }
    }
}

fun OkContext.runtimeDependenciesClosure(): OkAsync<Set<OkDependencyDeclaration>> {
    return launchMemoizedCoroutine(describeCoroutine("runtimeDependenciesClosure"), input = OkInput.none()) {
        parseDependenciesFile().await()?.declarations.orEmpty()
            .filter { it.isRuntime }
            .withClosure<OkDependencyDeclaration> closure@{ declaration ->
                val module = declaration.dependencyModulePath() ?: return@closure emptyList()
                withModule(module) {
                    parseDependenciesFile().await()?.declarations.orEmpty().filter { it.isRuntime }
                }
            }
    }
}
