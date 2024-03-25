package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.walk

sealed class OkOutput : Serializable

data object OkEmptyOutput : OkOutput() {
    private fun readResolve(): Any = OkEmptyOutput
}

data class OkOutputFile(val path: OkPath) : OkOutput(), Serializable {
    constructor(path: Path) : this(path.toOk())
}

data class OkOutputDirectory(val path: OkPath) : OkOutput() {
    constructor(path: Path) : this(path.toOk())
}

data class OkCompositeOutput(val values: List<OkOutput>) : OkOutput()

@OptIn(ExperimentalPathApi::class)
fun OkOutput.walkFiles(): Sequence<Path> {
    return sequence {
        when (this@walkFiles) {
            is OkCompositeOutput -> yieldAll(values.flatMap { it.walkFiles() })
            is OkEmptyOutput -> Unit
            is OkOutputDirectory -> yieldAll(path.toPath().walk())
            is OkOutputFile -> yield(path.toPath())
        }
    }
}