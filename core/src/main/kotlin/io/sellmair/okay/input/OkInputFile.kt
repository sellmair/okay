package io.sellmair.okay.input

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.directoryStateHash
import io.sellmair.okay.io.regularFileStateHash
import kotlinx.serialization.Serializable
import kotlin.io.path.isDirectory

fun OkPath.asInput(): OkInputFile = OkInputFile(this)

@Serializable
data class OkInputFile(val path: OkPath) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        val systemPath = path.system()
        return if (systemPath.isDirectory()) systemPath.directoryStateHash()
        else systemPath.regularFileStateHash()
    }
}

