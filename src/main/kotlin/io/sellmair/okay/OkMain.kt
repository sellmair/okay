@file:JvmName("OkMain")

package io.sellmair.okay

import io.sellmair.okay.kotlin.kotlinCompile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking(Dispatchers.Default) {
        with(OkContextImpl(this)) {
            kotlinCompile().await()
        }
    }
}
