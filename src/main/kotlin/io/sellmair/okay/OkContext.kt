@file:JvmName("OkContextKt")
@file:Suppress("FunctionName")

package io.sellmair.okay

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun ok(body: suspend OkContext.() -> Unit) {
    runBlocking(Dispatchers.Default + OkCoroutineStack(emptyList()) + OkCoroutineCache() + Job()) {
        with(OkContextImpl(CoroutineScope(coroutineContext))) {
            body()
        }
    }
}

interface OkContext {
    val cs: CoroutineScope
}

internal operator fun OkContext.plus(element: CoroutineContext.Element): OkContext {
    return OkContext(CoroutineScope(cs.coroutineContext + element))
}

fun OkContext(cs: CoroutineScope): OkContext =
    OkContextImpl(cs)


suspend fun <T> OkContext(body: suspend OkContext.() -> T): T {
    return coroutineScope {
        with(OkContext(this)) {
            body()
        }
    }
}

private class OkContextImpl(override val cs: CoroutineScope) : OkContext

