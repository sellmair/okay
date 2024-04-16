package io.sellmair.okay.kotlin

import io.sellmair.okay.fs.absolutePathString
import io.sellmair.okay.fs.createDirectories
import io.sellmair.okay.fs.deleteRecursively
import io.sellmair.okay.jvm.KotlinCompileJvmRequest
import io.sellmair.okay.jvm.KotlinCompileJvmResponse
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.io.File

internal fun executeKotlinCompile(request: KotlinCompileJvmRequest) = with(request) {
    outputDirectory.deleteRecursively()
    outputDirectory.createDirectories()

    val args = K2JVMCompilerArguments()
    args.noStdlib = true
    args.moduleName = moduleName
    args.classpathAsList = dependencies.map { File(it.absolutePathString()) }
    args.freeArgs += sources.map { it.absolutePathString() }
    args.destinationAsFile = File(outputDirectory.absolutePathString())

    K2JVMCompiler().exec(System.err, MessageRenderer.GRADLE_STYLE, *args.toArgumentStrings().toTypedArray())
    KotlinCompileJvmResponse(outputDirectory)
}