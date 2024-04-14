package io.sellmair.okay.dependency

import io.sellmair.okay.OkContext
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.path

data class OkDependencyDeclaration(
    val value: String,
    val isExported: Boolean,
    val isCompile: Boolean,
    val isRuntime: Boolean,
)

fun OkContext.moduleOrNull(dependencyDeclaration: OkDependencyDeclaration): OkPath? {
    if (dependencyDeclaration.value.startsWith("module://")) {
        return path(dependencyDeclaration.value.removePrefix("module://"))
    }
    return null
}