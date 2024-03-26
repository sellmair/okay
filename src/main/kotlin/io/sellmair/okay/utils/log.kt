package io.sellmair.okay.utils

import io.sellmair.okay.OkCoroutineStack
import io.sellmair.okay.OkModuleContext
import kotlinx.coroutines.currentCoroutineContext

suspend fun log(value: String) {
    val stack = currentCoroutineContext()[OkCoroutineStack]?.values ?: return
    val modulePath = currentCoroutineContext()[OkModuleContext]?.path?.path.orEmpty()

    println(
        buildString {
            if (modulePath.isNotBlank()) {
                append("{{$modulePath}} >> ")
            }

            append("[")
            append(stack.joinToString("/"))
            append("]")
            append(": $value")
        }
    )
}