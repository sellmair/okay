@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.io

import io.sellmair.okay.OkContext
import java.io.Serializable
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

/**
 * Represents a collection of regular/existing files
 */
sealed interface OkFileCollection {
    suspend fun resolve(ctx: OkContext): List<OkPath>
}

fun OkPath.walk(): OkFileCollection {
    return OkWalkFileCollection(this)
}

fun OkFileCollection.withExtension(extension: String): OkFileCollection {
    return OkExtensionFilteredFileCollection(this, extension)
}

@OptIn(ExperimentalPathApi::class)
private data class OkWalkFileCollection(
    private val root: OkPath
) : OkFileCollection, Serializable {
    override suspend fun resolve(ctx: OkContext): List<OkPath> = with(ctx) {
        root.system().walk()
            .filter { it.isRegularFile() }
            .map { it.ok() }
            .toList()
    }
}

private data class OkExtensionFilteredFileCollection(
    val source: OkFileCollection,
    val extension: String
) : OkFileCollection, Serializable {
    override suspend fun resolve(ctx: OkContext): List<OkPath> {
        return source.resolve(ctx).filter { it.extension == extension }
    }
}