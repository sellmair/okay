package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.copyFile
import io.sellmair.okay.io.toOk
import io.sellmair.okay.maven.mavenResolveRuntimeDependencies
import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.log
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name
import kotlin.io.path.relativeTo

suspend fun OkContext.kotlinPackage(): OkPath {
    val packageDir = modulePath("build/main/package")

    return cachedCoroutine(
        describeCoroutine("kotlinPackage", verbosity = Info),
        input = OkInput.none(),
        output = OkOutputDirectory(packageDir)
    ) {
        val mavenRuntimeDependencies = async { packageMavenRuntimeDependencies(packageDir) }
        val moduleDependencies = async { packageModuleDependencies(packageDir) }

        val classPath = async {
            (mavenRuntimeDependencies.await() + moduleDependencies.await()).map { path ->
                path.system().relativeTo(packageDir.system())
            }.joinToString(" ")
        }

        val mainClass = async {
            parseKotlinRunOptions().className
        }

        val jarFile = kotlinJar(
            jarManifestAttributes = mapOf(
                "Main-Class" to mainClass,
                "Class-Path" to classPath,
            )
        )

        copyFile(jarFile, packageDir.resolve(jarFile.system().name))
        log("Packaged Application in '$ansiGreen$packageDir$ansiReset'")
        packageDir
    }
}


suspend fun OkContext.packageModuleDependencies(packageDir: OkPath): List<OkPath> {
    val dependencyModules = runtimeDependenciesClosure().mapNotNull { it.dependencyModulePath() }
    val dependencyModuleJars = dependencyModules.map { module ->
        withModule(module) {
            async { kotlinJar() }
        }
    }

    return dependencyModuleJars.map { dependencyModuleJar ->
        async {
            val fromFile = dependencyModuleJar.await()
            val targetFile = packageDir.resolve("libs").resolve(fromFile.system().name)
            targetFile.system().createParentDirectories()
            copyFile(fromFile, targetFile)
        }
    }.awaitAll()
}

/* Copy maven dependencies into package/libs */
suspend fun OkContext.packageMavenRuntimeDependencies(packageDir: OkPath): List<OkPath> {
    val runtimeDependencies = mavenResolveRuntimeDependencies()
    val destinationFiles = runtimeDependencies.map { file ->
        packageDir.system().resolve("libs/${file.system().name}")
    }.map { it.toOk() }

    return cachedCoroutine(
        describeCoroutine("copyMavenRuntimeDependencies"),
        input = OkInputs(runtimeDependencies.map { OkInputFile(it) }),
        output = OkOutputs(destinationFiles.map { OkOutputFile(it) }),
    ) {
        runtimeDependencies.forEach { file ->
            val targetFile = packageDir.system().resolve("libs/${file.system().name}")
            targetFile.createParentDirectories()
            file.system().copyTo(targetFile, true)
        }

        destinationFiles
    }
}
