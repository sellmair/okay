package io.sellmair.okay.io

import io.sellmair.okay.*
import io.sellmair.okay.input.asInput
import io.sellmair.okay.output.asOutput
import kotlinx.serialization.serializer
import kotlin.io.path.copyTo

suspend fun OkContext.copyFile(from: OkPath, to: OkPath): OkPath {
    return cachedCoroutine(
        descriptor = describeCoroutine("copyFile"),
        input = from.asInput(),
        output = to.asOutput(),
        serializer = serializer()
    ) {
        from.system().copyTo(to.system(), true).ok()
    }
}