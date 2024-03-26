@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.toOk
import io.sellmair.okay.maven.mavenResolveDependencies
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.incremental.classpathAsList
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.nio.file.Path
import kotlin.io.path.*

suspend fun OkContext.kotlinCompile(): OkAsync<OkPath> {
    val mainSourcesDir = Path("src")
    val kotlinSources = mainSourcesDir.walk().filter { it.extension == "kt" }.toList()
    val dependencies = mavenResolveDependencies().await().map { okPath -> okPath.toPath() }

    return kotlinCompile(kotlinSources, dependencies, Path("build/main/classes"))
}

fun OkContext.kotlinCompile(
    sources: List<Path>,
    dependencies: List<Path>,
    outputDirectory: Path
): OkAsync<OkPath> {
    return cachedTask(
        "compile",
        input = OkCompositeInput(sources.map { OkFileInput(it) }) +
                OkCompositeInput(dependencies.map { OkFileInput(it) }),
        output = OkCompositeOutput(listOf(OkOutputDirectory(outputDirectory)))
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
