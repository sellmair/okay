@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.maven.mavenResolveCompileDependencies
import io.sellmair.okay.utils.log
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import kotlin.io.path.*

suspend fun OkContext.kotlinCompile(): OkPath {
    val kotlinSources = OkFileTree(modulePath("src"), filter = { it.extension == "kt" })

    val dependencies = mavenResolveCompileDependencies() +
            kotlinCompileDependencies()

    return kotlinCompile(kotlinSources, dependencies, modulePath("build/classes"))
}

suspend fun OkContext.kotlinCompile(
    sources: OkFileTree,
    dependencies: List<OkPath>,
    outputDirectory: OkPath
): OkPath {
    return cachedCoroutine(
        describeCoroutine("kotlinCompile", verbosity = Info),
        input = sources + OkInputs(dependencies.map { OkInputFile(it) }),
        output = OkOutputs(listOf(OkOutputDirectory(outputDirectory)))
    ) {
        log("Compiling Kotlin")

        outputDirectory.system().deleteRecursively()
        outputDirectory.system().createDirectories()

        val args = K2JVMCompilerArguments()
        args.noStdlib = true
        args.classpathAsList = dependencies.map { it.system().toFile() }
        args.freeArgs += sources.walk(ctx).map { it.system().absolutePathString() }
        args.destinationAsFile = outputDirectory.system().toFile()

        K2JVMCompiler.main(args.toArgumentStrings().toTypedArray())

        outputDirectory
    }
}
