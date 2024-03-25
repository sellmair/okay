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

fun readCacheEntry(key: OkHash): OkCacheEntry<*>? {
    val file = cacheEntriesDirectory.resolve(key.value)
    if (!file.isRegularFile()) return null

    return ObjectInputStream(file.inputStream().buffered()).use { stream ->
        stream.readObject() as? OkCacheEntry<*>
    }
}

private fun writeCacheEntry(key: OkHash, value: OkCacheEntry<*>): Path {
    cacheEntriesDirectory.createDirectories()

    val file = cacheEntriesDirectory.resolve(key.value)
    ObjectOutputStream(file.outputStream().buffered()).use { stream ->
        stream.writeObject(value)
    }

    return file
}

suspend fun <T> storeCache(
    key: OkHash,
    value: T,
    title: String,
    input: OkInput,
    output: OkOutput,
    dependencies: List<OkHash>
): OkCacheEntry<T> {
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

        val entry = OkCacheEntry(
            key = key,
            value = value,
            title = title,
            input = input,
            output = output,
            outputHash = outputHash.await(),
            dependencies = dependencies,
            files = files
        )

        writeCacheEntry(key, entry)
        entry
    }
}

suspend fun restoreFilesFromCache(entry: OkCacheEntry<*>) {
    withContext(Dispatchers.IO) {
        entry.output.withClosure { output -> if (output is OkCompositeOutput) output.values else emptyList() }
            .filterIsInstance<OkOutputDirectory>()
            .forEach { outputDirectory -> outputDirectory.path.toPath().deleteRecursively() }

        entry.files.map { (path, hash) ->
            async {
                val blob = cacheBlobsDirectory.resolve(hash.value)
                if (blob.isRegularFile()) {
                    path.toPath().createParentDirectories()
                    blob.copyTo(path.toPath(), true)
                }
            }
        }.map { it.await() }
    }
}

fun OkInput.cacheKey(): OkHash {
    return when (this) {
        is OkCompositeInput -> hash(values.map { it.cacheKey() })
        is OkStringInput -> hash(value)
        is OkFileInput -> path.toPath().regularFileCacheKey()
    }
}

fun OkOutput.cacheKey(): OkHash {
    return when (this) {
        is OkCompositeOutput -> hash(values.map { it.cacheKey() })
        is OkEmptyOutput -> hash("")
        is OkOutputDirectory -> path.toPath().directoryCacheKey()
        is OkOutputFile -> path.toPath().regularFileCacheKey()
    }
}

private fun Path.directoryCacheKey(): OkHash {
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

private fun Path.regularFileCacheKey(): OkHash {
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
