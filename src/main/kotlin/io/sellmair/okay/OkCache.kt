@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay

import io.sellmair.okay.io.toOk
import io.sellmair.okay.utils.withClosure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path
import kotlin.io.path.*

private val cacheDirectory = Path(".okay/cache")
private val cacheEntriesDirectory = cacheDirectory.resolve("entry")
private val cacheBlobsDirectory = cacheDirectory.resolve("blobs")

internal fun readCacheEntry(key: OkHash): OkInputCacheRecord? {
    val file = cacheEntriesDirectory.resolve(key.value)
    if (!file.isRegularFile()) return null

    return ObjectInputStream(file.inputStream().buffered()).use { stream ->
        stream.readObject() as? OkOutputCacheRecord<*>
    }
}


suspend fun <T> storeCache(
    key: OkHash,
    value: T,
    taskDescriptor: OkCoroutineDescriptor<T>,
    input: OkInput,
    output: OkOutput,
    dependencies: List<OkHash>
): OkOutputCacheRecord<T> {
    cacheBlobsDirectory.createDirectories()
    return withContext(Dispatchers.IO) {
        val outputHash = async { output.cacheKey() }

        val files = output.walkFiles()
            .toList()
            .map { path ->
                async {
                    if (!path.exists() || !path.isRegularFile()) return@async null
                    val fileCacheKey = path.regularFileCacheKey()
                    val blobFile = cacheBlobsDirectory.resolve(fileCacheKey.value)
                    path.copyTo(blobFile, true)
                    path.toOk() to fileCacheKey
                }
            }
            .mapNotNull { it.await() }
            .toMap()

        val entry = OkOutputCacheRecord(
            key = key,
            value = value,
            descriptor = taskDescriptor,
            input = input,
            output = output,
            outputHash = outputHash.await(),
            dependencies = dependencies,
            outputState = files
        )

        storeCacheRecord(entry)
        entry
    }
}

/**
 * ⚠️Only stores the cache record!!! Consider using [storeCache] instead?
 */
internal fun storeCacheRecord(value: OkInputCacheRecord): Path {
    cacheEntriesDirectory.createDirectories()

    val file = cacheEntriesDirectory.resolve(value.key.value)
    ObjectOutputStream(file.outputStream().buffered()).use { stream ->
        stream.writeObject(value)
    }

    return file
}

suspend fun restoreFilesFromCache(entry: OkOutputCacheRecord<*>) {
    withContext(Dispatchers.IO) {
        entry.output.withClosure { output -> if (output is OkOutputs) output.values else emptyList() }
            .filterIsInstance<OkOutputDirectory>()
            .forEach { outputDirectory -> outputDirectory.path.system().deleteRecursively() }

        entry.outputState.map { (path, hash) ->
            async {
                val blob = cacheBlobsDirectory.resolve(hash.value)
                if (blob.isRegularFile()) {
                    path.system().createParentDirectories()
                    blob.copyTo(path.system(), true)
                }
            }
        }.map { it.await() }
    }
}


fun OkOutput.cacheKey(): OkHash {
    return when (this) {
        is OkOutputs -> hash(values.map { it.cacheKey() })
        is OkEmptyOutput -> hash("")
        is OkOutputDirectory -> path.system().directoryCacheKey()
        is OkOutputFile -> path.system().regularFileCacheKey()
    }
}


internal fun Path.directoryCacheKey(): OkHash {
    return hash {
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

internal fun Path.regularFileCacheKey(): OkHash {
    return hash {
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
