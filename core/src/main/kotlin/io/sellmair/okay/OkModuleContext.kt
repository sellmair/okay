package io.sellmair.okay

import io.sellmair.okay.fs.OkFs
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.fs.absolutePathString
import okio.Path.Companion.toPath
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.pathString


fun OkContext.rootPath() = path("")

fun OkContext.modulePath(): OkPath {
    return cs.coroutineContext[OkModuleContext]?.path ?: rootPath()
}

fun OkContext.moduleName() = modulePath().absolutePathString().toPath(true).name

fun OkContext.modulePath(path: String): OkPath {
    return modulePath().resolve(path)
}

fun OkContext.path(value: String): OkPath {
    val root = cs.coroutineContext[OkRoot]?.path?.toString() ?: ""
    return OkFs(root).path(value)
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


internal class OkRoot(
    val path: Path
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<OkRoot>
}
