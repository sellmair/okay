@file:JvmName("OkContextKt")
@file:Suppress("FunctionName")

package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import kotlinx.coroutines.*
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun ok(body: suspend OkContext.() -> Unit) {
    runBlocking(Dispatchers.Default + OkCoroutineStack(emptyList()) + OkCoroutineCache() + Job()) {
        with(OkContextImpl(this)) {
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

    val ctx: OkContext get() = this

    fun Path.ok(): OkPath = path(this)
}

internal operator fun OkContext.plus(element: CoroutineContext.Element): OkContext {
    return OkContext(CoroutineScope(cs.coroutineContext + element))
}

fun OkContext(cs: CoroutineScope): OkContext =
    OkContextImpl(cs)


suspend fun <T> OkScope(body: suspend OkContext.() -> T): T {
    return coroutineScope {
        with(OkContext(this)) {
            body()
        }
    }
}

suspend fun <T> OkContext.withOkContext(
    ctx: CoroutineContext = EmptyCoroutineContext,
    action: suspend OkContext.() -> T
): T {
    val newCoroutineContext = cs.coroutineContext + ctx
    val okContext = OkContext(CoroutineScope(newCoroutineContext))
    return okContext.async { action(this@async) }.await()
}

private class OkContextImpl(
    override val cs: CoroutineScope
) : OkContext

