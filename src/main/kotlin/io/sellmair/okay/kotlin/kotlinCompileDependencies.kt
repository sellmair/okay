package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines

fun OkContext.kotlinCompileDependencies(): OkAsync<List<OkPath>> {
    val libsFile = modulePath("okay.libs")

    return cachedTask(
        describeTask("kotlinCompileDependencies"),
        input = OkFileInput(libsFile),
        output = OkOutput()
    ) {
        if (!libsFile.system().isRegularFile()) return@cachedTask emptyList()
        val modules = libsFile.system().readLines()
            .filter { it.startsWith("okay:") }
            .map { it.removePrefix("okay:").trim() }
            .map { path(it) }

        val transitiveDependencies = modules.map { modulePath ->
            withModule(modulePath) {
                kotlinCompileDependencies()
            }
        }

        val directDependencies = modules.map { modulePath ->
            withModule(modulePath) {
                kotlinCompile()
            }
        }

        transitiveDependencies.flatMap { it.await() } +
                directDependencies.map { it.await() }
    }
}