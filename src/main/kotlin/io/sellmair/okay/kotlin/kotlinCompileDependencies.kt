package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.io.OkPath

fun OkContext.kotlinCompileDependencies(): OkAsync<List<OkPath>> {
    return launchCachedCoroutine(
        describeCoroutine("kotlinCompileDependencies"),
        input = OkInput.none(),
        output = OkOutput.none()
    ) {
        val dependencyModulesClosure = compileDependenciesClosure().await()
            .mapNotNull { it.dependencyModulePath() }

        dependencyModulesClosure.map { dependencyModule ->
            withModule(dependencyModule) {
                kotlinCompile().await()
            }
        }
    }
}
