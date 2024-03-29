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

fun OkContext.kotlinPackage() {
    val packageDir = modulePath("build/main/package")
    val packagedMavenDependencies = packageMavenRuntimeDependenciesAsync(packageDir)
    val packagedModuleDependencies = packageModuleDependenciesAsync(packageDir)
    async {
        val jarFile = kotlinJar().await()
        copyFile(jarFile, packageDir.resolve(jarFile.system().name))
    }
}

fun OkContext.packageModuleDependenciesAsync(packageDir: OkPath): OkAsync<List<OkPath>> {
    return async {
        val dependencyModules = runtimeDependenciesClosure().await().mapNotNull { it.dependencyModulePath() }
        val dependencyModuleJars = dependencyModules.map { module -> withModule(module) { kotlinJar() } }
        dependencyModuleJars.map { dependencyModuleJar ->
            async {
                val fromFile = dependencyModuleJar.await()
                val targetFile = packageDir.resolve("libs").resolve(fromFile.system().name)
                targetFile.system().createParentDirectories()
                copyFile(fromFile, targetFile).await()
            }
        }.map { it.await() }
    }
}

/* Copy maven dependencies into package/libs */
fun OkContext.packageMavenRuntimeDependenciesAsync(packageDir: OkPath): OkAsync<List<OkPath>> {
    return async {
        val runtimeDependencies = mavenResolveRuntimeDependencies().await()
        val destinationFiles = runtimeDependencies.map { file ->
            packageDir.system().resolve("libs/${file.system().name}")
        }.map { it.toOk() }

        launchCachedCoroutine(
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
        }.await()
    }
}
