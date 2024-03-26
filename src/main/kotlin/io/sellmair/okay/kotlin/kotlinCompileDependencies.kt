package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines

fun OkContext.kotlinCompileDependencies(): OkAsync<List<OkPath>> {
    val libsFile = modulePath("okay.libs")

    return cachedTask(
        "compile dependencies",
        input = OkFileInput(libsFile) + OkStringInput("compile dependencies of") + OkStringInput(modulePath().path),
        output = OkOutput()
    ) {
        if (!libsFile.system().isRegularFile()) return@cachedTask emptyList()
        val modules = libsFile.system().readLines()
            .filter { it.startsWith("okay:") }
            .map { it.removePrefix("okay:").trim() }
            .map { path(it) }

        modules.map { modulePath ->
            withModule(modulePath) {
                kotlinCompile()
            }
        }.map { it.await() }
    }
}