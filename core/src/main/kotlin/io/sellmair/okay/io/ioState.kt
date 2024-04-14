package io.sellmair.okay.io

import io.sellmair.okay.OkHash
import io.sellmair.okay.fs.*
import io.sellmair.okay.hash
import io.sellmair.okay.okCacheDispatcher
import kotlinx.coroutines.withContext


internal suspend fun OkPath.directoryStateHash(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        if (isDirectory()) {
            list().map { entry ->
                if (entry.isDirectory()) {
                    push(entry.directoryStateHash())
                } else if (entry.isRegularFile()) {
                    push(entry.regularFileStateHash())
                }
            }
        }
    }
}

internal suspend fun OkPath.regularFileStateHash(): OkHash = withContext(okCacheDispatcher) {
    hash {
        push(absolutePathString())
        push(if (isRegularFile()) 1 else 0)

        if (isRegularFile()) {
            push(source())
        }
    }
}