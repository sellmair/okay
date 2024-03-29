package io.sellmair.okay.utils

import io.sellmair.okay.OkCoroutineStack
import io.sellmair.okay.OkModuleContext
import io.sellmair.okay.OkCoroutineDescriptor
import kotlinx.coroutines.currentCoroutineContext

internal const val ansiReset = "\u001B[0m"
internal const val ansiCyan = "\u001B[36m"
internal const val ansiGreen = "\u001B[32m"
internal const val ansiPurple = "\u001B[35m"
internal const val ansiYellow = "\u001B[33m"

suspend fun log(value: String) {
    val stackElement = currentCoroutineContext()[OkCoroutineStack]?.values.orEmpty()
        .lastOrNull { it.verbosity >= OkCoroutineDescriptor.Verbosity.Info }
        ?: return

    val title = stackElement.let { "$ansiPurple${it.title}$ansiReset" }
    val modulePath = stackElement.module.path.ifBlank { "<root>" }

    println(
        buildString {
            if (modulePath.isNotBlank()) {
                append("{{${ansiCyan}$modulePath${ansiReset}}} $ansiYellow>>$ansiReset ")
            }

            append("[")
            append(title)
            append("]")
            append(": $value")
        }
    )
}
