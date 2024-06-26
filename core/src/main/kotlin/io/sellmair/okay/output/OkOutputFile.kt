package io.sellmair.okay.output

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.io.regularFileStateHash
import java.io.Serializable

fun OkPath.asOutput(): OkOutputFile = OkOutputFile(this)

@kotlinx.serialization.Serializable
data class OkOutputFile(val path: OkPath) : OkOutput, Serializable {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return path.regularFileStateHash()
    }
}