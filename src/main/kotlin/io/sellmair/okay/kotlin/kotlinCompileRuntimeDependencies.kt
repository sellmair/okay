package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.withModule

fun OkContext.kotlinCompileRuntimeDependencies(): OkAsync<List<OkPath>> {
    return async {
        val dependencyModulesClosure = runtimeDependenciesClosure().await()
            .mapNotNull { it.dependencyModulePath() }

        dependencyModulesClosure.map { dependencyModule ->
            withModule(dependencyModule) {
                kotlinCompile().await()
            }
        }
    }
}