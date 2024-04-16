package io.sellmair.okay.jvm

import io.sellmair.okay.OkContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

interface OkJvmClient : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    suspend fun send(request: JvmRequest): JvmResponse

    companion object Key : CoroutineContext.Key<OkJvmClient>
}


suspend inline fun <reified T : JvmResponse> JvmRequest.execute(): T {
    return ((currentCoroutineContext()[OkJvmClient] ?: error("Missing OkJvmClient")).send(this)).orThrow()
}

expect fun CoroutineScope.OkJvmClient(): OkJvmClient
