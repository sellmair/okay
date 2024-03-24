@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.maven.mavenResolveDependency
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.nio.file.Path
import kotlin.io.path.*

suspend fun OkBuildContext.kotlinCompile(): OkAsync<Path> {
    val mainSourcesDir = Path("src")
    val kotlinSources = mainSourcesDir.walk().filter { it.extension == "kt" }.toList()

    val dependencies = listOf(
        mavenResolveDependency("org.jetbrains.kotlin", "kotlin-stdlib", "1.9.23"),
        mavenResolveDependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", "1.8.0"),
    ).map { it.await() }

    return kotlinCompile(kotlinSources, dependencies, Path("out/main"))
}

fun OkBuildContext.kotlinCompile(
    sources: List<Path>,
    dependencies: List<Path>,
    outputDirectory: Path
): OkAsync<Path> {
    return cached(
        "compile",
        input = OkCompositeInput(sources.map { OkFileInput(it) }),
        output = OkCompositeOutput(listOf(OkOutputDirectory(outputDirectory)))
    ) {
        log("Compiling Kotlin")

        outputDirectory.createDirectories()

        val args = K2JVMCompilerArguments()
        args.noStdlib = true
        args.classpathAsList = dependencies.map { it.toFile() }
        args.freeArgs += sources.map { it.absolutePathString() }
        args.destinationAsFile = outputDirectory.toFile()

        val arguments = args.toArgumentStrings()
        log("arguments=${arguments.joinToString("\n")}")

        K2JVMCompiler.main(args.toArgumentStrings().toTypedArray())

        outputDirectory
    }
}
