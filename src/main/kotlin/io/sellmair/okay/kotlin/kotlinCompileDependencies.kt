package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.dependency.moduleOrNull
import io.sellmair.okay.io.OkPath

suspend fun OkContext.kotlinCompileDependencies(): List<OkPath> {
    val dependencyModulesClosure = compileDependenciesClosure()
        .mapNotNull { moduleOrNull(it) }

    return dependencyModulesClosure.map { dependencyModule ->
        withOkModule(dependencyModule) {
            async { kotlinCompile() }
        }
    }.awaitAll()
}
