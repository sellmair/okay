package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.walk

sealed class OkOutput : Serializable {
    companion object {
        fun none(): OkOutput = OkOutputs(emptyList())
    }
}

data object OkEmptyOutput : OkOutput() {
    private fun readResolve(): Any = OkEmptyOutput
}

data class OkOutputFile(val path: OkPath) : OkOutput(), Serializable {
    constructor(path: Path) : this(path.toOk())
}

data class OkOutputDirectory(val path: OkPath) : OkOutput() {
    constructor(path: Path) : this(path.toOk())
}

fun OkOutput(vararg output: OkOutput): OkOutput {
    val outputs = output.toList()
    if (outputs.isEmpty()) return OkEmptyOutput
    return OkOutputs(outputs)
}

data class OkOutputs(val values: List<OkOutput>) : OkOutput()

@OptIn(ExperimentalPathApi::class)
fun OkOutput.walkFiles(): Sequence<Path> {
    return sequence {
        when (this@walkFiles) {
            is OkOutputs -> yieldAll(values.flatMap { it.walkFiles() })
            is OkEmptyOutput -> Unit
            is OkOutputDirectory -> yieldAll(path.system().walk())
            is OkOutputFile -> yield(path.system())
        }
    }
}