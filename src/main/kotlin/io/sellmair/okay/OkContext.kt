@file:JvmName("OkContextKt")
@file:Suppress("FunctionName")

package io.sellmair.okay

import kotlinx.coroutines.*

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

