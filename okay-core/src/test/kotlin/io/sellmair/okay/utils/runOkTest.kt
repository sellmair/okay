package io.sellmair.okay.utils

import io.sellmair.okay.OkContext
import io.sellmair.okay.ok
import io.sellmair.okay.withOkContext
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun runOkTest(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend OkContext.() -> Unit
) {
    runTest {
        println("Running ok test")
        ok {
            withOkContext(OkTestLogger() + OkTestCoroutineCacheHook() + coroutineContext) {
                block()
            }
        }
    }
}

