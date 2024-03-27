package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import java.io.Serializable
import kotlin.reflect.typeOf

data class OkCoroutineDescriptor<T>(
    val id: String,
    val title: String,
    val module: OkPath,
    val verbosity: Verbosity,
    val signatureOfT: String
) : Serializable, OkInput() {
    enum class Verbosity {
        Silent, Debug, Info
    }

    override fun currentState(): OkHash {
        return hash {
            push(id)
            push(title)
            push(module.path)
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
        id = id, title = title, module = rootModulePath(), verbosity, signatureOfT = typeOf<T>().toString()
    )
}