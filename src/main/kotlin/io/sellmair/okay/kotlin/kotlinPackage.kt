package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.dependency.runtimeDependenciesClosure
import io.sellmair.okay.io.OkPath
import io.sellmair.okay.io.copyFile
import io.sellmair.okay.io.toOk
import io.sellmair.okay.maven.mavenResolveRuntimeDependencies
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name

suspend fun OkContext.kotlinPackage() {
    val packageDir = modulePath("build/main/package")
    async { packageMavenRuntimeDependencies(packageDir) }
    async { packageModuleDependencies(packageDir) }
    async {
        val jarFile = kotlinJar()
        copyFile(jarFile, packageDir.resolve(jarFile.system().name))
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
        input = OkCompositeInput(runtimeDependencies.map { OkFileInput(it) }),
        output = OkCompositeOutput(destinationFiles.map { OkOutputFile(it) }),
    ) {
        runtimeDependencies.forEach { file ->
            val targetFile = packageDir.system().resolve("libs/${file.system().name}")
            targetFile.createParentDirectories()
            file.system().copyTo(targetFile, true)
        }

        destinationFiles
    }
}
