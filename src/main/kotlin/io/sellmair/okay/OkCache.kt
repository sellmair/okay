package io.sellmair.okay

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path
import kotlin.io.path.*


fun readCacheEntry(key: OkHash): OkCacheEntry<*>? {
    val file = Path("cache/entry/$key")
    if (!file.isRegularFile()) return null

    return ObjectInputStream(file.inputStream().buffered()).use { stream ->
        stream.readObject() as? OkCacheEntry<*>
    }
}

private fun writeCacheEntry(key: OkHash, value: OkCacheEntry<*>): Path {
    val base = Path("cache/entry")
    base.createDirectories()

    val file = base.resolve(key.value)
    ObjectOutputStream(file.outputStream().buffered()).use { stream ->
        stream.writeObject(value)
    }

    return file
}


suspend fun <T> storeCache(inputHash: OkHash, value: T, output: OkOutput): Path {
    val base = Path("cache/blob")
    base.createDirectories()

    return withContext(Dispatchers.IO) {
        val files = output.walkFiles()
            .toList()
            .map { path ->
                async {
                    if (!path.exists() || !path.isRegularFile()) return@async null
                    val key = path.regularFileCacheKey()
                    val blobFile = base.resolve(key.value)
                    path.copyTo(blobFile, true)
                    path to key
                }
            }
            .mapNotNull { it.await() }
            .toMap()

        val entryHash = output.cacheKey()

        val entry = OkCacheEntry(
            value = value,
            outputHash = entryHash,
            files = files
        )

        writeCacheEntry(inputHash, entry)
    }
}

suspend fun restoreFilesFromCache(entry: OkCacheEntry<*>) {
    withContext(Dispatchers.IO) {
        entry.files.map { (path, hash) ->
            async {
                val blob = Path("cache/blob/$hash")
                if (blob.isRegularFile()) {
                    blob.copyTo(path, true)
                }
            }
        }.map { it.await() }
    }
}

fun OkInput.cacheKey(): OkHash {
    return when (this) {
        is OkCompositeInput -> hash(values.map { it.cacheKey() })
        is OkStringInput -> hash(value)
        is OkFileInput -> path.regularFileCacheKey()
    }
}

fun OkOutput.cacheKey(): OkHash {
    return when (this) {
        is OkCompositeOutput -> hash(values.map { it.cacheKey() })
        is OkEmptyOutput -> hash("")
        is OkOutputDirectory -> path.directoryCacheKey()
        is OkOutputFile -> path.regularFileCacheKey()
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
