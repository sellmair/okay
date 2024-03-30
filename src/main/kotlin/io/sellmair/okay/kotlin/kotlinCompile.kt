@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import io.sellmair.okay.maven.mavenResolveCompileDependencies
import io.sellmair.okay.utils.log
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.nio.file.Path
import kotlin.io.path.*

suspend fun OkContext.kotlinCompile(): OkPath  {
    val mainSourcesDir = modulePath("src").system()
    val kotlinSources = mainSourcesDir.walk().filter { it.extension == "kt" }.toList()
    val dependencies = mavenResolveCompileDependencies().map { it.system() } +
            kotlinCompileDependencies().map { it.system() }

    return kotlinCompile(kotlinSources, dependencies, modulePath("build/classes").system())
}

suspend fun OkContext.kotlinCompile(
    sources: List<Path>,
    dependencies: List<Path>,
    outputDirectory: Path
): OkPath {
    return cachedCoroutine(
        describeCoroutine("kotlinCompile", verbosity = Info),
        input = OkInputs(sources.map { OkInputFile(it) }) +
                OkInputs(dependencies.map { OkInputFile(it) }),
        output = OkOutputs(listOf(OkOutputDirectory(outputDirectory)))
    ) {
        log("Compiling Kotlin")

        outputDirectory.deleteRecursively()
        outputDirectory.createDirectories()

        val args = K2JVMCompilerArguments()
        args.noStdlib = true
        args.classpathAsList = dependencies.map { it.toFile() }
        args.freeArgs += sources.map { it.absolutePathString() }
        args.destinationAsFile = outputDirectory.toFile()

        K2JVMCompiler.main(args.toArgumentStrings().toTypedArray())

        outputDirectory.toOk()
    }
}
