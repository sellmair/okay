package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import kotlin.coroutines.CoroutineContext


fun OkContext.path(value: String) = OkPath(value)

fun OkContext.rootModulePath() = OkPath("")

fun OkContext.modulePath(): OkPath {
    return cs.coroutineContext[OkModuleContext]?.path ?: OkPath("")
}

fun OkContext.modulePath(path: String): OkPath {
    return modulePath().resolve(path)
}

internal inline fun <T> OkContext.withModule(path: OkPath, block: OkContext.() -> T): T {
    return with(this + OkModuleContext(path)) {
        block()
    }
}

internal class OkModuleContext(
    val path: OkPath
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkModuleContext>
}
