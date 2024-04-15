package io.sellmair.okay.clean

import io.sellmair.okay.OkContext
import io.sellmair.okay.fs.deleteRecursively
import io.sellmair.okay.okExtensions
import io.sellmair.okay.path
import io.sellmair.okay.utils.log
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
suspend fun OkContext.okClean() {
    log("Cleaning .okay")
    path(".okay").deleteRecursively()

    okExtensions<OkCleanExtension> { extension ->
        extension.clean(this)
    }
}