@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import io.sellmair.okay.zip.zipFiles
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

suspend fun OkContext.kotlinJar(
    jarManifestAttributes: Map<String, OkAsync<String>> = emptyMap(),
): OkPath {
    val outputDir = kotlinCompile()

    val files = outputDir.system().walk().associate { file ->
        val relativePath = file.relativeTo(outputDir.system())
        relativePath.toString() to file.toOk()
    }

    val manifest = buildString {
        appendLine("Manifest-Version: 1.0")
        jarManifestAttributes.forEach { (key, value) ->
            appendLine("$key: ${value.await()}")
        }
    }

    return zipFiles(
        modulePath("build/main/jar/${moduleName()}.jar"),
        data = mapOf("META-INF/MANIFEST.MF" to manifest.encodeToByteArray()),
        files = files,
    )
}
