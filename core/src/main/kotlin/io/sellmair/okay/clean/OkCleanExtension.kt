package io.sellmair.okay.clean

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkExtension

interface OkCleanExtension: OkExtension {
    suspend fun clean(ctx: OkContext)
}
