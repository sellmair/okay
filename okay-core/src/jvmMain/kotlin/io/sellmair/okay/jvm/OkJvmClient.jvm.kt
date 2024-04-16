package io.sellmair.okay.jvm

import io.sellmair.okay.execute
import kotlinx.coroutines.CoroutineScope

actual fun CoroutineScope.OkJvmClient(): OkJvmClient = object : OkJvmClient {
    override suspend fun send(request: JvmRequest): JvmResponse {
        return execute(request)
    }
}