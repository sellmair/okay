package io.sellmair.okay

import io.sellmair.okay.jvm.*
import io.sellmair.okay.kotlin.executeKotlinCompile
import io.sellmair.okay.serialization.format
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.DataInputStream
import java.io.DataOutputStream

fun main(): Unit = runBlocking {
    val input = DataInputStream(System.`in`)
    val output = DataOutputStream(System.out)

    val responseChannel = Channel<JvmResponsePkg>()

    /* Handle incoming requests */
    launch(Dispatchers.IO) {
        while (true) {
            System.err.println("Reading... ")
            val size = input.readInt()

            System.err.println("Pkg size: $size")
            val requestPkg = format.decodeFromByteArray<JvmRequestPkg>(input.readNBytes(size))
            System.err.println("Request: $requestPkg")
            launch(Dispatchers.Default) {
                responseChannel.send(JvmResponsePkg(requestPkg.requestId, execute(requestPkg.request)))
            }
        }
    }

    /* Send responses */
    launch(Dispatchers.IO) {
        responseChannel.consumeEach { response ->
            System.err.println("Sending $response")
            val responseBytes = format.encodeToByteArray(response)
            output.writeInt(responseBytes.size)
            output.write(responseBytes)
            output.flush()
        }
    }
}

internal fun execute(request: JvmRequest): JvmResponse {
    return try {
        when (request) {
            is KotlinCompileJvmRequest -> executeKotlinCompile(request)
        }
    } catch (t: Throwable) {
        ErrorJvmResponse(t.message.orEmpty(), t.stackTrace.map { it.toString() })
    }
}
