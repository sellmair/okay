package io.sellmair.okay.dependency

import io.sellmair.okay.io.OkPath

data class OkDependenciesFile(
    val module: OkPath,
    val declarations: List<OkDependencyDeclaration>
)