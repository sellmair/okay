package io.sellmair.okay.kotlin

import io.sellmair.okay.OkContext
import io.sellmair.okay.async
import io.sellmair.okay.awaitAll
import io.sellmair.okay.dependency.compileDependenciesClosure
import io.sellmair.okay.dependency.moduleOrNull
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.withOkModule

suspend fun OkContext.kotlinCompileDependencies(): List<OkPath> {
    val dependencyModulesClosure = compileDependenciesClosure()
        .mapNotNull { moduleOrNull(it) }

    return dependencyModulesClosure.map { dependencyModule ->
        withOkModule(dependencyModule) {
            async { kotlinCompile() }
        }
    }.awaitAll()
}
