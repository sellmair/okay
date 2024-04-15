package io.sellmair.okay.utils

import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.test.fail

class OkTestLogger : OkLogger {
    private val logs = mutableListOf<Log>()
    private val lock = ReentrantLock()

    data class Log(val module: String, val title: String, val value: String)

    override fun log(module: String, title: String, value: String) {
        lock.withLock {
            logs.add(Log(module, title, value))
            OkStdLogger.log(module, title, value)
        }
    }

    fun logs() = lock.withLock {
        logs.toList()
    }

    fun clearLogs() = lock.withLock {
        logs.clear()
    }
}

internal suspend fun clearLogs() {
    (currentCoroutineContext()[OkLogger] as OkTestLogger).clearLogs()
}

internal suspend fun logs(): List<OkTestLogger.Log> =
    (currentCoroutineContext()[OkLogger] as OkTestLogger).logs()

internal suspend fun assertContainsLog(module: String, title: String, value: String) {
    if (logs().contains(OkTestLogger.Log(module, title, value))) return
    throw AssertionError("Log not found: module=$module, title=$title, value=$value")
}

internal suspend fun assertNoDuplicateLogs() {
    val logs = logs()
    val grouped = logs.groupBy { it }.mapValues { (_, all) -> all.size }
    val duplicates = grouped.filter { (log, count) -> count > 1 }
    if (duplicates.isEmpty()) return

    fail(buildString {
        appendLine("Duplicate logs found:")
        duplicates.forEach { (log, count) ->
            appendLine("Found $count times: $log")
        }
    })
}

internal suspend fun assertNoDuplicateMessages() {
    val logs = logs()
    val grouped = logs.groupBy { it.value }.mapValues { (_, all) -> all.size }
    val duplicates = grouped.filter { (_, count) -> count > 1 }
    if (duplicates.isEmpty()) return

    fail(buildString {
        appendLine("Duplicate logs found:")
        duplicates.forEach { (log, count) ->
            appendLine("Found $count times: $log")
        }
    })
}