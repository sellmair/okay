@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.OkAsync
import io.sellmair.okay.OkContext
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import io.sellmair.okay.moduleName
import io.sellmair.okay.modulePath
import io.sellmair.okay.zip.zipFiles
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

suspend fun OkContext.kotlinJar(): OkAsync<OkPath> {
    val outputDir = kotlinCompile().await()
    val files = outputDir.system().walk().associate { file ->
        val relativePath = file.relativeTo(outputDir.system())
        relativePath.toString() to file.toOk()
    }

    return zipFiles(
        modulePath("build/main/jar/${moduleName()}.jar"), files
    )
}
