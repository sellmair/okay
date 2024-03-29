package io.sellmair.okay.dependency

import io.sellmair.okay.*
import io.sellmair.okay.utils.withClosure
import io.sellmair.okay.withModule

suspend fun OkContext.compileDependenciesClosure(): Set<OkDependencyDeclaration> {
    return memoizedCoroutine(describeCoroutine("compileDependenciesClosure"), input = OkInput.none()) {
        parseDependenciesFile()?.declarations.orEmpty()
            .filter { it.isCompile }
            .withClosure<OkDependencyDeclaration> closure@{ declaration ->
                val module = declaration.dependencyModulePath() ?: return@closure emptyList()
                withModule(module) {
                    parseDependenciesFile()?.declarations.orEmpty().filter { it.isExported && it.isCompile }
                }
            }
    }
}

suspend fun OkContext.runtimeDependenciesClosure(): Set<OkDependencyDeclaration> {
    return memoizedCoroutine(describeCoroutine("runtimeDependenciesClosure"), input = OkInput.none()) {
        parseDependenciesFile()?.declarations.orEmpty()
            .filter { it.isRuntime }
            .withClosure<OkDependencyDeclaration> closure@{ declaration ->
                val module = declaration.dependencyModulePath() ?: return@closure emptyList()
                withModule(module) {
                    parseDependenciesFile()?.declarations.orEmpty().filter { it.isRuntime }
                }
            }
    }
}
