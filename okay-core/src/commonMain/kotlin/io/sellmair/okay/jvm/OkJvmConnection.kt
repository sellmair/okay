package io.sellmair.okay.jvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

fun interface OkJvmConnection : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    suspend fun send(request: JvmRequest): JvmResponse

    companion object Key : CoroutineContext.Key<OkJvmConnection>
}

suspend inline fun <reified T : JvmResponse> JvmRequest.execute(): T {
    return ((currentCoroutineContext()[OkJvmConnection] ?: error("Missing OkJvmConnection")).send(this)).orThrow()
}

expect fun CoroutineScope.OkJvmConnection(): OkJvmConnection
