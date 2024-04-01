package io.sellmair.okay.clean

import io.sellmair.okay.OkContext
import io.sellmair.okay.okExtensions
import io.sellmair.okay.path
import io.sellmair.okay.utils.log
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

@OptIn(ExperimentalPathApi::class)
suspend fun OkContext.okClean() {
    log("Cleaning .okay")
    path(".okay").system().deleteRecursively()

    okExtensions<OkCleanExtension> { extension ->
        extension.clean(this)
    }
}