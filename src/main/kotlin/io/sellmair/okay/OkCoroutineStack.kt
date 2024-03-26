package io.sellmair.okay

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

val CoroutineContext.okStack: List<String>
    get() = (this[OkCoroutineStack] ?: error("Missing 'OkCoroutineStack'")).values

fun CoroutineContext.pushOkStack(next: String): CoroutineContext {
    return OkCoroutineStack(okStack + next)
}

data class OkCoroutineStack(val values: List<String>) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkCoroutineStack>
}

suspend fun log(value: String) {
    val stack = currentCoroutineContext()[OkCoroutineStack]?.values ?: return
    println("[${stack.joinToString("/")}]: $value")
}