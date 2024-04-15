package io.sellmair.okay.input

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.fs.isDirectory
import io.sellmair.okay.io.directoryStateHash
import io.sellmair.okay.io.regularFileStateHash
import kotlinx.serialization.Serializable

fun OkPath.asInput(): OkInputFile = OkInputFile(this)

@Serializable
data class OkInputFile(val path: OkPath) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return if (path.isDirectory()) path.directoryStateHash()
        else path.regularFileStateHash()
    }
}

