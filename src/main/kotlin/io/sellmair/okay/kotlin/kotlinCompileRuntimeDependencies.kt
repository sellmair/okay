package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.withModule

suspend fun OkContext.kotlinCompileRuntimeDependencies(): List<OkPath> {
    val dependencyModulesClosure = runtimeDependenciesClosure()
        .mapNotNull { it.dependencyModulePath() }

    return dependencyModulesClosure.map { dependencyModule ->
        withModule(dependencyModule) {
            async { kotlinCompile() }
        }
    }.awaitAll()
}