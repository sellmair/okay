package io.sellmair.okay.io

import io.sellmair.okay.*
import kotlin.io.path.copyTo

suspend fun OkContext.copyFile(from: OkPath, to: OkPath): OkPath {
    return cachedCoroutine(
        descriptor = describeCoroutine("copyFile"),
        input = from.asInput(),
        output = to.asOutput()
    ) {
        from.system().copyTo(to.system(), true).ok()
    }
}