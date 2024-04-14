@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.fs.absolutePathString
import io.sellmair.okay.fs.createDirectories
import io.sellmair.okay.fs.deleteRecursively
import io.sellmair.okay.input.asInput
import io.sellmair.okay.input.plus
import io.sellmair.okay.io.OkFileCollection
import io.sellmair.okay.io.walk
import io.sellmair.okay.io.withExtension
import io.sellmair.okay.maven.mavenResolveCompileDependencies
import io.sellmair.okay.output.OkOutputDirectory
import io.sellmair.okay.utils.log
import okio.Path.Companion.toPath
import org.jetbrains.kotlin.cli.common.CLITool
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.io.File
import kotlin.io.path.*

suspend fun OkContext.kotlinCompile(): OkPath {
    val kotlinSources = modulePath("src").walk().withExtension("kt")

    val dependencies = mavenResolveCompileDependencies() +
            kotlinCompileDependencies()

    return kotlinCompile(kotlinSources, dependencies, modulePath("build/classes"))
}

suspend fun OkContext.kotlinCompile(
    sources: OkFileCollection,
    dependencies: List<OkPath>,
    outputDirectory: OkPath
): OkPath {

    return cachedCoroutine(
        describeCoroutine("kotlinCompile", verbosity = Info),
        input = sources.asInput() +
                dependencies.map { it.asInput() }.asInput(),
        output = OkOutputDirectory(outputDirectory),
        serializer = OkPath.serializer()
    ) {
        log("Compiling Kotlin")

        outputDirectory.deleteRecursively()
        outputDirectory.createDirectories()

        val args = K2JVMCompilerArguments()
        args.noStdlib = true
        args.moduleName = moduleName()
        args.classpathAsList = dependencies.map { File(it.absolutePathString()) }
        args.freeArgs += sources.resolve(ctx).map { it.absolutePathString() }
        args.destinationAsFile = File(outputDirectory.absolutePathString())

        K2JVMCompiler().exec(System.err, MessageRenderer.GRADLE_STYLE, *args.toArgumentStrings().toTypedArray())
        outputDirectory
    }
}
