package io.sellmair.okay.dependency

import io.sellmair.okay.fs.OkPath

data class OkDependenciesFile(
    val module: OkPath,
    val declarations: List<OkDependencyDeclaration>
)