package io.sellmair.okay

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> withOkStack(element: String, block: suspend () -> T): T {
    return withContext(currentCoroutineContext().pushOkStack(element)) {
        block()
    }
}

val CoroutineContext.okStack: List<String>
    get() = (this[OkCoroutineStack] ?: error("Missing 'OkCoroutineStack'")).values

fun CoroutineContext.pushOkStack(next: String): CoroutineContext {
    return OkCoroutineStack(okStack + next)
}


data class OkCoroutineStack(val values: List<String>) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkCoroutineStack>
}

