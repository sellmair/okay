package io.sellmair.okay.io

import io.sellmair.okay.OkContext
import io.sellmair.okay.cachedCoroutine
import io.sellmair.okay.describeCoroutine
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.fs.copyTo
import io.sellmair.okay.input.asInput
import io.sellmair.okay.output.asOutput
import kotlinx.serialization.serializer

suspend fun OkContext.copyFile(from: OkPath, to: OkPath): OkPath {
    return cachedCoroutine(
        descriptor = describeCoroutine("copyFile"),
        input = from.asInput(),
        output = to.asOutput(),
        serializer = serializer()
    ) {
        from.copyTo(to)
    }
}