package io.sellmair.okay

import java.nio.file.Path

sealed class OkInput

data class OkFileInput(val path: Path) : OkInput()

data class OkStringInput(val value: String) : OkInput()

data class OkCompositeInput(val values: List<OkInput>) : OkInput()

