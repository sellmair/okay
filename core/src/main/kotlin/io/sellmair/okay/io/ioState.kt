package io.sellmair.okay.io

import io.sellmair.okay.OkHash
import io.sellmair.okay.hash
import io.sellmair.okay.okCacheDispatcher
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.*


internal suspend fun Path.directoryStateHash(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        if (isDirectory()) {
            listDirectoryEntries().map { entry ->
                if (entry.isDirectory()) {
                    push(entry.directoryStateHash())
                } else if (entry.isRegularFile()) {
                    push(entry.regularFileStateHash())
                }
            }
        }
    }
}

internal suspend fun Path.regularFileStateHash(): OkHash = withContext(okCacheDispatcher) {
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