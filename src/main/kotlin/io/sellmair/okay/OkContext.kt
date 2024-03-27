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

/**
 * ⚠️Maybe this should be changed to also implement CoroutineScope direclty?
 * This is used mostly by OkContext {} to provide a scope.
 * Furthermore `suspend fun OkContext.foo()`  might be easy to mess up:
 * What coroutine context is correct? The cs.coroutineContext or currentCoroutineContext()?
 */
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

