package io.sellmair.okay

import io.sellmair.okay.io.OkPath
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

data class OkFileTree(
    val root: OkPath, val filter: (OkPath) -> Boolean
) : OkInput() {
    override suspend fun cacheKey(ctx: OkContext): OkHash {
        return hash {
            walk(ctx).forEach { regularFile ->
                push(OkInputFile(regularFile).cacheKey(ctx))
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    fun walk(ctx: OkContext): Sequence<OkPath> = with(ctx) {
        return root.system().walk()
            .filter { path -> path.isRegularFile() }
            .map { path -> path.ok() }
            .filter(filter)
    }
}
