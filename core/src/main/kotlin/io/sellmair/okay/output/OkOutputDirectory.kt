package io.sellmair.okay.output

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.directoryStateHash

data class OkOutputDirectory(val path: OkPath) : OkOutput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return path.system().directoryStateHash()
    }
}