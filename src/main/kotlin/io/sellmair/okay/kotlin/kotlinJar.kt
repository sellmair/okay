@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import io.sellmair.okay.zip.zipFiles
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

fun OkContext.kotlinJar(): OkAsync<OkPath> = async {
    val outputDir = kotlinCompile().await()

    val files = outputDir.system().walk().associate { file ->
        val relativePath = file.relativeTo(outputDir.system())
        relativePath.toString() to file.toOk()
    }

    zipFiles(
        modulePath("build/main/jar/${moduleName()}.jar"), files
    ).await()
}
