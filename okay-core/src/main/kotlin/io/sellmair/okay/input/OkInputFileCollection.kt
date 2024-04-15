package io.sellmair.okay.input

import io.sellmair.okay.OkContext
import io.sellmair.okay.OkHash
import io.sellmair.okay.hash
import io.sellmair.okay.io.OkFileCollection
import io.sellmair.okay.io.regularFileStateHash
import kotlinx.serialization.Serializable

fun OkFileCollection.asInput(): OkInputFileCollection =
    OkInputFileCollection(this)

@Serializable
data class OkInputFileCollection(val files: OkFileCollection) : OkInput {
    override suspend fun currentHash(ctx: OkContext): OkHash {
        return hash {
            files.resolve(ctx).forEach { path ->
                push(path.regularFileStateHash())
            }
        }
    }
}