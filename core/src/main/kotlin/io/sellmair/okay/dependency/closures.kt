package io.sellmair.okay.dependency

import io.sellmair.okay.*
import io.sellmair.okay.utils.log
import io.sellmair.okay.utils.withClosure

suspend fun OkContext.compileDependenciesClosure(): Set<OkDependencyDeclaration> {
    return memoizedCoroutine(describeCoroutine("compileDependenciesClosure"), input = OkInput.none()) {
        parseDependenciesFile()?.declarations.orEmpty().plus(kotlinStdlibDependencyDeclaration)
            .filter { it.isCompile }
            .withClosure<OkDependencyDeclaration> closure@{ declaration ->
                val module = moduleOrNull(declaration) ?: return@closure emptyList()
                withOkModule(module) {
                    parseDependenciesFile()?.declarations.orEmpty().filter { it.isExported && it.isCompile }
                }
            }
    }
}

suspend fun OkContext.runtimeDependenciesClosure(): Set<OkDependencyDeclaration> {
    return memoizedCoroutine(describeCoroutine("runtimeDependenciesClosure"), input = OkInput.none()) {
        parseDependenciesFile()?.declarations.orEmpty().plus(kotlinStdlibDependencyDeclaration)
            .filter { it.isRuntime }
            .withClosure<OkDependencyDeclaration> closure@{ declaration ->
                val module = moduleOrNull(declaration) ?: return@closure emptyList()
                withOkModule(module) {
                    parseDependenciesFile()?.declarations.orEmpty().filter { it.isRuntime }
                }
            }
    }
}

private val kotlinStdlibDependencyDeclaration = OkDependencyDeclaration(
    "org.jetbrains.kotlin:kotlin-stdlib:1.9.23",
    isCompile = true,
    isExported = false,
    isRuntime = true
)