package io.sellmair.okay

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> withOkStack(descriptor: OkTaskDescriptor<*>, block: suspend () -> T): T {
    return withContext(currentCoroutineContext().pushOkStack(descriptor)) {
        block()
    }
}

val CoroutineContext.okStack: List<OkTaskDescriptor<*>>
    get() = (this[OkCoroutineStack] ?: error("Missing 'OkCoroutineStack'")).values

fun CoroutineContext.pushOkStack(next: OkTaskDescriptor<*>): CoroutineContext {
    return OkCoroutineStack(okStack + next)
}


data class OkCoroutineStack(val values: List<OkTaskDescriptor<*>>) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkCoroutineStack>
}

