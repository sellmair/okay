package io.sellmair.okay.utils

import io.sellmair.okay.OkCoroutineDescriptor.Verbosity
import io.sellmair.okay.okStack
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

internal const val ansiReset = "\u001B[0m"
internal const val ansiCyan = "\u001B[36m"
internal const val ansiGreen = "\u001B[32m"
internal const val ansiPurple = "\u001B[35m"
internal const val ansiYellow = "\u001B[33m"

suspend fun log(value: String) {
    val stack = currentCoroutineContext().okStack
    val lastFrame = stack.lastOrNull { it.verbosity >= Verbosity.Info } ?: return
    val title = lastFrame.title
    val module = lastFrame.module.toString().ifBlank { "<root>" }

    val logger = currentCoroutineContext()[OkLogger] ?: OkStdLogger
    logger.log(module, title, value)
}

interface OkLogger : CoroutineContext.Element {

    fun log(module: String, title: String, value: String)

    override val key: CoroutineContext.Key<*> get() = Key

    companion object Key : CoroutineContext.Key<OkLogger>
}

internal object OkStdLogger : OkLogger {
    override fun log(module: String, title: String, value: String) {
        println(
            buildString {
                append("{{${ansiCyan}$module${ansiReset}}} $ansiYellow>>$ansiReset ")
                append("[")
                append("$ansiPurple${title}$ansiReset")
                append("]")
                append(": $value")
            }
        )
    }
}