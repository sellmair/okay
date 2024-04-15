@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.io

import io.sellmair.okay.OkContext
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.fs.isRegularFile
import io.sellmair.okay.fs.listRecursively
import java.io.Serializable
import kotlin.io.path.ExperimentalPathApi

/**
 * Represents a collection of regular/existing files
 */
@kotlinx.serialization.Serializable
sealed interface OkFileCollection {
    suspend fun resolve(ctx: OkContext): List<OkPath>
}

fun OkPath.walk(): OkFileCollection {
    return OkWalkFileCollection(this)
}

fun OkFileCollection.withExtension(extension: String): OkFileCollection {
    return OkExtensionFilteredFileCollection(this, extension)
}

@kotlinx.serialization.Serializable
internal data class OkWalkFileCollection(
    private val root: OkPath
) : OkFileCollection, Serializable {
    override suspend fun resolve(ctx: OkContext): List<OkPath> = with(ctx) {
        root.listRecursively()
            .filter { it.isRegularFile() }
            .toList()
    }
}

@kotlinx.serialization.Serializable
internal data class OkExtensionFilteredFileCollection(
    val source: OkFileCollection,
    val extension: String
) : OkFileCollection, Serializable {
    override suspend fun resolve(ctx: OkContext): List<OkPath> {
        return source.resolve(ctx).filter { it.extension == extension }
    }
}
