package io.sellmair.okay.utils

import io.sellmair.okay.OkCoroutineStack
import io.sellmair.okay.OkModuleContext
import io.sellmair.okay.OkCoroutineDescriptor
import kotlinx.coroutines.currentCoroutineContext

const val ansiReset = "\u001B[0m"
const val ansiCyan = "\u001B[36m"
const val ansiGreen = "\u001B[32m"

suspend fun log(value: String) {
    val stack = currentCoroutineContext()[OkCoroutineStack]?.values.orEmpty()
        .filter { it.verbosity >= OkCoroutineDescriptor.Verbosity.Info }
        .map { it.title }
        .ifEmpty { return }

    val modulePath = currentCoroutineContext()[OkModuleContext]?.path?.path ?: "<root>"

    println(
        buildString {
            if (modulePath.isNotBlank()) {
                append("{{${ansiCyan}$modulePath${ansiReset}}} >> ")
            }

            append("[")
            append(stack.joinToString("/"))
            append("]")
            append(": $value")
        }
    )
}
