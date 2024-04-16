package io.sellmair.okay

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.sellmair.okay.jvm.*
import io.sellmair.okay.kotlin.executeKotlinCompile
import io.sellmair.okay.serialization.format
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalCoroutinesApi::class)

fun main(args: Array<String>) {
    runBlocking(SupervisorJob() + Dispatchers.Default.limitedParallelism(1)) {
        run(args)
    }
}

suspend fun run(args: Array<String>) = coroutineScope {
    val port = args.first().toInt()
    val socket = aSocket(SelectorManager(Dispatchers.IO)).tcp().connect(hostname = "127.0.0.1", port = port)
    val responseChannel = Channel<JvmResponsePkg>()

    /* Read from socket and dispatch tasks */
    launch(Dispatchers.IO) {
        val readChannel = socket.openReadChannel()

        while (isActive) {
            try {
                val nextPkgSize = readChannel.readInt()
                val binary = readChannel.readPacket(nextPkgSize).readBytes()
                val requestPkg = format.decodeFromByteArray<JvmRequestPkg>(binary)

                launch(Dispatchers.Default) {
                    val response = execute(requestPkg.request)
                    responseChannel.send(JvmResponsePkg(requestPkg.requestId, response))
                }
            } catch (t: ClosedReceiveChannelException) {
                this@coroutineScope.coroutineContext.cancelChildren()
            }
        }
    }

    /* Take responses and send them back to the main process */
    launch(Dispatchers.IO) {
        val writeChannel = socket.openWriteChannel()
        responseChannel.consumeEach { responsePkg ->
            val binary = format.encodeToByteArray(responsePkg)
            writeChannel.writeInt(binary.size)
            writeChannel.writeFully(binary)
            writeChannel.flush()
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
