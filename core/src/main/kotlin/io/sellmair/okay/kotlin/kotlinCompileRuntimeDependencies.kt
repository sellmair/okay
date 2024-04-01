package io.sellmair.okay.kotlin

import io.sellmair.okay.OkContext
import io.sellmair.okay.async
import io.sellmair.okay.awaitAll
import io.sellmair.okay.dependency.moduleOrNull
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.withOkModule

suspend fun OkContext.kotlinCompileRuntimeDependencies(): List<OkPath> {
    val dependencyModulesClosure = runtimeDependenciesClosure()
        .mapNotNull { moduleOrNull(it) }

    return dependencyModulesClosure.map { dependencyModule ->
        withOkModule(dependencyModule) {
            async { kotlinCompile() }
        }
    }.awaitAll()
}