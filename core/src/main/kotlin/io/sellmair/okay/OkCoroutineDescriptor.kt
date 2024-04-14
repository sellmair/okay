package io.sellmair.okay

import io.sellmair.okay.input.OkInput
import io.sellmair.okay.fs.OkPath
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class OkCoroutineDescriptor<T>(
    val id: String,
    val title: String,
    val module: OkPath,
    val verbosity: Verbosity,
    val signatureOfT: String
): OkInput {
    enum class Verbosity {
        Silent, Debug, Info
    }

    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash {
            push(id)
            push(title)
            push(module)
            push(verbosity.name)
            push(signatureOfT)
        }
    }
}

inline fun <reified T> OkContext.describeCoroutine(
    id: String,
    title: String = id,
    verbosity: OkCoroutineDescriptor.Verbosity = OkCoroutineDescriptor.Verbosity.Debug
): OkCoroutineDescriptor<T> {
    return OkCoroutineDescriptor(
        id = id, title = title, module = modulePath(), verbosity, signatureOfT = typeOf<T>().toString()
    )
}

inline fun <reified T> OkContext.describeRootCoroutine(
    id: String,
    title: String = id,
    verbosity: OkCoroutineDescriptor.Verbosity = OkCoroutineDescriptor.Verbosity.Debug
): OkCoroutineDescriptor<T> {
    return OkCoroutineDescriptor(
        id = id, title = title, module = rootPath(), verbosity, signatureOfT = typeOf<T>().toString()
    )
}