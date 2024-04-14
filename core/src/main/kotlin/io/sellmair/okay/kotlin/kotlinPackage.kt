package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.dependency.moduleOrNull
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.fs.*
import io.sellmair.okay.input.OkInput
import io.sellmair.okay.input.OkInputFile
import io.sellmair.okay.input.OkInputProperty
import io.sellmair.okay.input.OkInputs
import io.sellmair.okay.io.copyFile
import io.sellmair.okay.maven.mavenResolveRuntimeDependencies
import io.sellmair.okay.output.OkOutput
import io.sellmair.okay.output.OkOutputFile
import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.log
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.*

suspend fun OkContext.kotlinPackage(): OkPath {
    val packageDir = modulePath("build/application")

    return cachedCoroutine(
        describeCoroutine("kotlinPackage", verbosity = Info),
        input = OkInput.none(),
        output = OkOutput.none()
    ) {
        val mavenRuntimeDependencies = async { packageMavenRuntimeDependencies(packageDir) }
        val moduleDependencies = async { packageModuleDependencies(packageDir) }
        async { packageStartScript(packageDir) }

        val classPath = async {
            (mavenRuntimeDependencies.await() + moduleDependencies.await()).map { path ->
                path.relativeTo(packageDir)
            }.joinToString(" ")
        }

        val mainClass = async {
            parseKotlinRunOptions()?.className ?: "MainKt"
        }

        val jarFile = kotlinJar(
            jarManifestAttributes = mapOf(
                "Main-Class" to mainClass,
                "Class-Path" to classPath,
            )
        )

        copyFile(jarFile, packageDir.resolve(jarFile.name))
        log("Packaged Application in '$ansiGreen$packageDir$ansiReset'")
        packageDir
    }
}


suspend fun OkContext.packageModuleDependencies(packageDir: OkPath): List<OkPath> {
    val dependencyModules = runtimeDependenciesClosure().mapNotNull { moduleOrNull(it) }
    val dependencyModuleJars = dependencyModules.map { module ->
        withOkModule(module) {
            async { kotlinJar() }
        }
    }

    return dependencyModuleJars.map { dependencyModuleJar ->
        async {
            val fromFile = dependencyModuleJar.await()
            val targetFile = packageDir.resolve("libs").resolve(fromFile.name)
            targetFile.createParentDirectories()
            copyFile(fromFile, targetFile)
        }
    }.awaitAll()
}

/* Copy maven dependencies into package/libs */
suspend fun OkContext.packageMavenRuntimeDependencies(packageDir: OkPath): List<OkPath> {
    val runtimeDependencies = mavenResolveRuntimeDependencies()
    val destinationFiles = runtimeDependencies.map { file ->
        packageDir.resolve("libs/${file.name}")
    }

    return cachedCoroutine(
        describeCoroutine("copyMavenRuntimeDependencies"),
        input = OkInputs(runtimeDependencies.map { OkInputFile(it) }),
        output = OkOutput.none(),
    ) {
        runtimeDependencies.forEach { file ->
            val targetFile = packageDir.resolve("libs/${file.name}")
            targetFile.createParentDirectories()
            file.copyTo(targetFile)
        }

        destinationFiles
    }
}

suspend fun OkContext.packageStartScript(packageDir: OkPath): OkPath {
    val scriptFile = packageDir.resolve(moduleName())
    val dollar = "$"
    val scriptContent = """
        #!/bin/bash
        script_dir=$(dirname $0)
        java -jar ${dollar}script_dir/${moduleName()}.jar
    """.trimIndent()

    return cachedCoroutine(
        describeCoroutine("packageStartScript"),
        input = OkInputProperty("scriptContent", scriptContent),
        output = OkOutputFile(scriptFile)
    ) {
        scriptFile.createParentDirectories()
        scriptFile.writeText(scriptContent)
        scriptFile.setIsExecutable(true)

        scriptFile
    }
}