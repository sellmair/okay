@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.clean.OkCleanExtension
import io.sellmair.okay.dependency.moduleOrNull
import io.sellmair.okay.dependency.parseDependenciesFile
import io.sellmair.okay.fs.deleteRecursively
import io.sellmair.okay.utils.log
import kotlin.io.path.ExperimentalPathApi

internal class KotlinCleanExtension : OkCleanExtension {
    override suspend fun clean(ctx: OkContext) {
        ctx.kotlinClean()
    }
}

internal suspend fun OkContext.kotlinClean() {
    withOkStack(descriptor = describeCoroutine<Unit>("kotlinClean", verbosity = Info)) {
        val dependencyModules = parseDependenciesFile()?.declarations.orEmpty()
            .mapNotNull { moduleOrNull(it) }

        dependencyModules.map { module ->
            async { withOkModule(module) { kotlinClean() } }
        }.awaitAll()

        log("Cleaning Kotlin")
        modulePath("build").deleteRecursively()
    }
}
