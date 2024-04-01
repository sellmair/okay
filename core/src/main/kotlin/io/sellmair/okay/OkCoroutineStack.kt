package io.sellmair.okay

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> OkContext.withOkStack(descriptor: OkCoroutineDescriptor<*>, block: suspend OkContext.() -> T): T {
    return withOkContext(currentCoroutineContext().pushOkStack(descriptor), block)
}

val CoroutineContext.okStack: List<OkCoroutineDescriptor<*>>
    get() = (this[OkCoroutineStack] ?: error("Missing 'OkCoroutineStack'")).values

fun CoroutineContext.pushOkStack(next: OkCoroutineDescriptor<*>): CoroutineContext {
    return OkCoroutineStack(okStack + next)
}

data class OkCoroutineStack(val values: List<OkCoroutineDescriptor<*>>) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkCoroutineStack>
}
