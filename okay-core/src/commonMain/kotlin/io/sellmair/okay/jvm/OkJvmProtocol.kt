package io.sellmair.okay.jvm

import io.sellmair.okay.fs.OkPath
import kotlinx.serialization.Serializable

@Serializable
data class JvmRequestPkg(
    val requestId: Int,
    val request: JvmRequest
)

@Serializable
data class JvmResponsePkg(
    val requestId: Int,
    val response: JvmResponse
)

@Serializable
sealed class JvmRequest

@Serializable
sealed class JvmResponse

class ErrorJvmResponse(
    val message: String,
    val stackTrace: List<String>
) : JvmResponse()


@Serializable
data class KotlinCompileJvmRequest(
    val moduleName: String,
    val sources: List<OkPath>,
    val dependencies: List<OkPath>,
    val outputDirectory: OkPath
) : JvmRequest()


@Serializable
data class KotlinCompileJvmResponse(
    val outputDirectory: OkPath
) : JvmResponse()


inline fun <reified T : JvmResponse> JvmResponse.orThrow(): T {
    if (this is ErrorJvmResponse) {
        throw JvmRequestException(this.message + "\n" + this.stackTrace.joinToString())
    }

    return this as T
}

class JvmRequestException(
    message: String
) : Exception()