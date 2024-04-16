package io.sellmair.okay.jvm

import io.sellmair.okay.serialization.format
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.decodeFromByteArray
import okio.Buffer
import platform.posix.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
actual fun CoroutineScope.OkJvmClient(): OkJvmClient {
    val requestChannel = Channel<JvmRequestPkg>(Channel.BUFFERED)
    val responseChannel = Channel<JvmResponsePkg>(Channel.BUFFERED)
    val sharedResponseChannel = responseChannel.consumeAsFlow()
        .shareIn(this, SharingStarted.Eagerly, replay = 128 /* Magic */)

    launch {


        memScoped {
            val inputPipeFds = allocArray<IntVar>(2)
            val outputPipeFds = allocArray<IntVar>(2)

            delay(2.seconds)



            pipe(inputPipeFds)
            pipe(outputPipeFds)


            val pid = fork()


            // child
            if (pid == 0) {
                dup2(outputPipeFds[0], STDIN_FILENO)
                dup2(inputPipeFds[1], STDOUT_FILENO)

                close(outputPipeFds[1])
                close(inputPipeFds[0])

                system("java -jar okay.jar")
                println("Java fin")
            }

            //close unused pipe ends
            close(outputPipeFds[0])
            close(inputPipeFds[1])

            val output = outputPipeFds[1]
            val input = inputPipeFds[0]

            /* Write the requests to the process */
            launch(Dispatchers.IO) {
                requestChannel.consumeEach { requestPkg ->
                    val requestBinary = format.encodeToByteArray(JvmRequestPkg.serializer(), requestPkg)

                    val buffer = Buffer()
                    buffer.writeInt(requestBinary.size)
                    buffer.write(requestBinary)
                    val pkgBinary = buffer.readByteArray()

                    write(output, pkgBinary.toCValues(), pkgBinary.size.convert())
                }
            }

            /* Read responses from the process */
            launch(Dispatchers.IO) {
                val buffer = Buffer()
                val rawBuffer = allocArray<ByteVar>(128)
                while (true) {

                    val read = read(input, rawBuffer, 128.convert())
                    println("Native: got $read bytes")
                    val receivedData = rawBuffer.readBytes(read.convert())
                    buffer.write(receivedData)

                    val nextPkgSize = buffer.peek().readInt()
                    println("Native: nextPkgSize=$nextPkgSize; Buffer: ${buffer.size}$")
                    if (buffer.size >= nextPkgSize) {
                        buffer.readInt()
                        val responseBinary = buffer.readByteArray(nextPkgSize.convert())
                        val response = format.decodeFromByteArray<JvmResponsePkg>(responseBinary)
                        println("Native: got $response")
                        responseChannel.send(response)
                    }
                }
            }

            val pipeReader = outputPipeFds[0]

            // Create a coroutine to read from the pipe and print the output
            launch(Dispatchers.IO) {
                val buffer = ByteArray(4096)
                while (true) {
                    val bytesRead = read(pipeReader, buffer.refTo(0), 4096.convert())
                    if (bytesRead > 0) {
                        println(buffer.copyOfRange(0, bytesRead.convert()).decodeToString())
                    } else {
                        // Handle pipe closure or error
                        break
                    }
                }
            }
        }
    }

    val requestId = kotlin.concurrent.AtomicInt(0)

    return object : OkJvmClient {
        override suspend fun send(request: JvmRequest): JvmResponse {
            val currentRequestId = requestId.getAndIncrement()
            val requestPkg = JvmRequestPkg(currentRequestId, request)
            requestChannel.send(requestPkg)
            return sharedResponseChannel.first { it.requestId == currentRequestId }.response
        }
    }
}