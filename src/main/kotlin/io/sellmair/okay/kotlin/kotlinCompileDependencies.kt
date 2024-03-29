package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.io.OkPath

fun OkContext.kotlinCompileDependencies(): OkAsync<List<OkPath>> {
    return async {
        val dependencyModulesClosure = compileDependenciesClosure().await()
            .mapNotNull { it.dependencyModulePath() }

        dependencyModulesClosure.map { dependencyModule ->
            withModule(dependencyModule) {
                kotlinCompile().await()
            }
        }
    }
}
