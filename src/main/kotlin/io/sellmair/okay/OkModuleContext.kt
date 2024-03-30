package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.name


fun OkContext.path(value: String) = OkPath(value)

fun OkContext.rootModulePath() = OkPath("")

fun OkContext.modulePath(): OkPath {
    return cs.coroutineContext[OkModuleContext]?.path ?: OkPath("")
}

fun OkContext.moduleName() = modulePath().system().toAbsolutePath()
    .normalize().name

fun OkContext.modulePath(path: String): OkPath {
    return modulePath().resolve(path)
}

internal suspend fun <T> OkContext.withOkModule(path: OkPath, block: suspend OkContext.() -> T): T {
    return withOkContext(OkModuleContext(path)) {
        block()
    }
}

internal class OkModuleContext(
    val path: OkPath
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkModuleContext>
}
