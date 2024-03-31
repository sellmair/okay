package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.*

sealed class OkOutput : Serializable {
    companion object {
        fun none(): OkOutput = OkOutputs(emptyList())
    }
}

data object OkEmptyOutput : OkOutput() {
    private fun readResolve(): Any = OkEmptyOutput
}

fun OkPath.asOutput(): OkOutputFile = OkOutputFile(this)

data class OkOutputFile(val path: OkPath) : OkOutput(), Serializable

data class OkOutputDirectory(val path: OkPath) : OkOutput()

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

internal suspend fun OkOutput.cacheKey(): OkHash = withContext(okCacheDispatcher) {
    when (this@cacheKey) {
        is OkOutputs -> hash(values.map { it.cacheKey() })
        is OkEmptyOutput -> hash("")
        is OkOutputDirectory -> path.system().directoryCacheKey()
        is OkOutputFile -> path.system().regularFileCacheKey()
    }
}

internal suspend fun Path.directoryCacheKey(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        if (isDirectory()) {
            listDirectoryEntries().map { entry ->
                if (entry.isDirectory()) {
                    push(entry.directoryCacheKey())
                } else if (entry.isRegularFile()) {
                    push(entry.regularFileCacheKey())
                }
            }
        }
    }
}

internal suspend fun Path.regularFileCacheKey(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        push(if (exists()) 1 else 0)

        if (isRegularFile()) {
            val buffer = ByteArray(2048)
            inputStream().buffered().use { input ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    push(buffer, 0, read)
                }
            }
        }
    }
}
