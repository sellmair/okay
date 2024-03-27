package io.sellmair.okay.dependency

import io.sellmair.okay.io.OkPath

data class OkDependencyDeclaration(
    val value: String,
    val isExported: Boolean,
    val isCompile: Boolean,
    val isRuntime: Boolean,
) {
    fun dependencyModulePath(): OkPath? {
        if (value.startsWith("module://")) {
            return OkPath(value.removePrefix("module://"))
        }

        return null
    }
}