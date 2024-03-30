package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo


@OptIn(OkUnsafe::class)
fun OkContext.path(value: String): OkPath {
    val root = cs.coroutineContext[OkRoot]?.path?.toString() ?: ""
    return OkPath(root, value)
}

@OptIn(OkUnsafe::class)
fun OkContext.rootPath() = path("")

fun OkContext.modulePath(): OkPath {
    return cs.coroutineContext[OkModuleContext]?.path ?: rootPath()
}

fun OkContext.moduleName() = modulePath().system().toAbsolutePath()
    .normalize().name

fun OkContext.modulePath(path: String): OkPath {
    return modulePath().resolve(path)
}


@OptIn(OkUnsafe::class)
fun OkContext.path(path: Path): OkPath {
    val root = cs.coroutineContext[OkRoot]?.path ?: Path("")
    return OkPath(root.toString(), path.relativeTo(root).toString())
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