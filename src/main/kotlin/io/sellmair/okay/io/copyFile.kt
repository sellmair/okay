package io.sellmair.okay.io

import io.sellmair.okay.*
import kotlin.io.path.copyTo

fun OkContext.copyFile(from: OkPath, to: OkPath): OkAsync<OkPath> {
    return launchCachedCoroutine(
        descriptor = describeCoroutine("copyFile"),
        input = OkFileInput(from),
        output = OkOutputFile(to)
    ) {
        from.system().copyTo(to.system(), true).toOk()
    }
}