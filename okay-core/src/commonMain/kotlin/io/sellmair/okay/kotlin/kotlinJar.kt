package io.sellmair.okay.kotlin

import io.sellmair.okay.OkContext
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.fs.listRecursively
import io.sellmair.okay.moduleName
import io.sellmair.okay.modulePath
import io.sellmair.okay.zip.zipFiles

suspend fun OkContext.kotlinJar(
    jarManifestAttributes: Map<String, io.sellmair.okay.OkAsync<String>> = emptyMap(),
): OkPath {
    val outputDir = kotlinCompile()

    val files = outputDir.listRecursively().associateBy { file ->
        file.relativeTo(outputDir)
    }

    val manifest = buildString {
        appendLine("Manifest-Version: 1.0")
        jarManifestAttributes.forEach { (key, value) ->
            appendLine("$key: ${value.await().chunked(42).joinToString("\n")}")
        }
    }

    return zipFiles(
        modulePath("build/jar/${moduleName()}.jar"),
        data = mapOf("META-INF/MANIFEST.MF" to manifest.encodeToByteArray()),
        files = files,
    )
}
