package io.sellmair.okay.jvm

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.sellmair.okay.serialization.format
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import platform.posix.fork
import platform.posix.system
import kotlin.system.exitProcess


@OptIn(ExperimentalForeignApi::class)
actual fun CoroutineScope.OkJvmConnection(): OkJvmConnection {
    val serverSocket = aSocket(SelectorManager(Dispatchers.IO)).tcp().bind("127.0.0.1")
    val port = (serverSocket.localAddress as InetSocketAddress).port

    if (fork() == 0) {
        println("Port: $port")
        exitProcess(system("java -jar okay.jar $port"))
    }

    val deferred = async(Dispatchers.IO) {
        val socket = serverSocket.accept()
        this@OkJvmConnection.OkJvmConnection(socket)
    }

    return OkJvmConnectionWrapper(deferred)
}

fun CoroutineScope.OkJvmConnection(socket: Socket): OkJvmConnection {
    val requestChannel = Channel<JvmRequestPkg>()
    val responseChannel = Channel<JvmResponsePkg>()

    val responses = responseChannel.consumeAsFlow()
        .shareIn(this, started = SharingStarted.Eagerly)

    coroutineContext.job.invokeOnCompletion {
        socket.close()
    }

    launch(Dispatchers.IO) {
        val writeChannel = socket.openWriteChannel()
        requestChannel.consumeEach { requestPkg ->
            val binary = format.encodeToByteArray(requestPkg)
            println("Native: sending: $requestPkg")
            writeChannel.writeInt(binary.size)
            writeChannel.writeFully(binary)
            writeChannel.flush()
        }
    }

    launch(Dispatchers.IO) {
        val readChannel = socket.openReadChannel()
        val nextPkgSize = readChannel.readInt()
        val binary = readChannel.readPacket(nextPkgSize).readBytes()
        val response = format.decodeFromByteArray<JvmResponsePkg>(binary)
        println("Native: received: ${response}")
        responseChannel.send(response)
    }

    val requestId = atomic(0)

    return OkJvmConnection { request ->
        println("Native. dispatching request...")
        val currentRequestId = requestId.getAndIncrement()
        launch { requestChannel.send(JvmRequestPkg(currentRequestId, request)) }
        responses.first { it.requestId == currentRequestId }.response
    }
}

private class OkJvmConnectionWrapper(private val deferred: Deferred<OkJvmConnection>) : OkJvmConnection {
    override suspend fun send(request: JvmRequest): JvmResponse {
        return deferred.await().send(request)
    }
}
