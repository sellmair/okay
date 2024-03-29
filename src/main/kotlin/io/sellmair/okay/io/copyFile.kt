package io.sellmair.okay.io

import io.sellmair.okay.*
import kotlin.io.path.copyTo

suspend fun OkContext.copyFile(from: OkPath, to: OkPath): OkPath {
    return cachedCoroutine(
        descriptor = describeCoroutine("copyFile"),
        input = OkInputFile(from),
        output = OkOutputFile(to)
    ) {
        from.system().copyTo(to.system(), true).toOk()
    }
}