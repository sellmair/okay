package io.sellmair.okay

import io.sellmair.okay.input.OkInput
import io.sellmair.okay.io.regularFileStateHash
import io.sellmair.okay.output.OkOutput
import io.sellmair.okay.output.OkOutputDirectory
import io.sellmair.okay.output.OkOutputFile
import io.sellmair.okay.output.OkOutputs
import io.sellmair.okay.serialization.format
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import kotlin.io.path.*


/**
 * Stores a [OkCacheRecord]
 * @param inputHash The current state of the [input]
 * @param outputValue The result of the cached coroutine
 * @param descriptor The descriptor of the cached coroutine
 * @param input The input of the cached coroutine
 * @param output The output of the cached coroutine. This output will be traversed and all files associated
 * with the output will be captured in the [cacheBlobsDirectory]
 * @param dependencies The dependencies of the coroutine
 */
suspend fun <T> OkContext.storeCachedCoroutine(
    descriptor: OkCoroutineDescriptor<T>,
    input: OkInput,
    inputHash: OkHash,
    output: OkOutput,
    outputValue: T,
    serializer: KSerializer<T>,
    dependencies: Set<OkHash>
): OkCacheRecord = withOkContext(Dispatchers.IO) {
    cacheBlobsDirectory.system().createDirectories()

    val outputFiles = output.walkFiles()
        .toList()
        .mapNotNull { path ->
            if (!path.exists() || !path.isRegularFile()) return@mapNotNull null
            val fileCacheKey = path.regularFileStateHash()
            val blobFile = cacheBlobsDirectory.resolve(fileCacheKey.value).system()

            try {
                blobFile.createFile()
                path.copyTo(blobFile, true)
            } catch (t: FileAlreadyExistsException) {
                /* File exists, no need to store it */
            }

            path.ok() to fileCacheKey
        }
        .toMap()

    val entry = OkCacheRecord(
        session = currentOkSessionId(),
        descriptor = descriptor as OkCoroutineDescriptor<Any?>,
        input = input,
        inputHash = inputHash,
        output = output,
        outputHash = output.currentHash(ctx),
        payload = format.encodeToByteArray(serializer, outputValue),
        outputFiles = outputFiles,
        dependencies = dependencies.toSet(),
    )
    @OptIn(OkUnsafe::class)
    storeCacheRecord(entry)
    entry
}

@OptIn(ExperimentalPathApi::class)
fun OkOutput.walkFiles(): Sequence<Path> {
    return sequence {
        when (this@walkFiles) {
            is OkOutputs -> yieldAll(values.flatMap { it.walkFiles() })
            is OkOutputDirectory -> yieldAll(path.system().walk())
            is OkOutputFile -> yield(path.system())
        }
    }
}