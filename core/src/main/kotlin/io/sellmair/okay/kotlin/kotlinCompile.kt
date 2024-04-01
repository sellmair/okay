@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.io.OkFileCollection
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.walk
import io.sellmair.okay.io.withExtension
import io.sellmair.okay.maven.mavenResolveCompileDependencies
import io.sellmair.okay.utils.log
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

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
                OkInputs(dependencies.map { it.asInput() }) +
                OkInputString(moduleName()),
        output = OkOutputs(listOf(OkOutputDirectory(outputDirectory)))
    ) {
        log("Compiling Kotlin")

        outputDirectory.system().deleteRecursively()
        outputDirectory.system().createDirectories()

        val args = K2JVMCompilerArguments()
        args.noStdlib = true
        args.moduleName = moduleName()
        args.classpathAsList = dependencies.map { it.system().toFile() }
        args.freeArgs += sources.resolve(ctx).map { it.system().absolutePathString() }
        args.destinationAsFile = outputDirectory.system().toFile()

        K2JVMCompiler.main(args.toArgumentStrings().toTypedArray())

        outputDirectory
    }
}
