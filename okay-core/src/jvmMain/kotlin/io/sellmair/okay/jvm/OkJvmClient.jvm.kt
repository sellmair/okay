package io.sellmair.okay.jvm

import io.sellmair.okay.execute
import kotlinx.coroutines.CoroutineScope

actual fun CoroutineScope.OkJvmConnection(): OkJvmConnection = OkJvmConnection { request -> execute(request) }