package io.sellmair.okay

import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.walk

sealed class OkOutput

data object OkEmptyOutput : OkOutput()

data class OkOutputFile(val path: Path) : OkOutput(), Serializable

data class OkOutputDirectory(val path: Path) : OkOutput()

data class OkCompositeOutput(val values: List<OkOutput>) : OkOutput()

@OptIn(ExperimentalPathApi::class)
fun OkOutput.walkFiles(): Sequence<Path> {
    return sequence {
        when (this@walkFiles) {
            is OkCompositeOutput -> yieldAll(values.flatMap { it.walkFiles() })
            is OkEmptyOutput -> Unit
            is OkOutputDirectory -> yieldAll(path.walk())
            is OkOutputFile -> yield(path)
        }
    }
}