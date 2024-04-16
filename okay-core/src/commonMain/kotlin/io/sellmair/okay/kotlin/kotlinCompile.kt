package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.fs.OkPath
import io.sellmair.okay.input.asInput
import io.sellmair.okay.input.plus
import io.sellmair.okay.io.OkFileCollection
import io.sellmair.okay.io.walk
import io.sellmair.okay.io.withExtension
import io.sellmair.okay.jvm.KotlinCompileJvmRequest
import io.sellmair.okay.jvm.KotlinCompileJvmResponse
import io.sellmair.okay.jvm.execute
import io.sellmair.okay.maven.mavenResolveCompileDependencies
import io.sellmair.okay.output.OkOutputDirectory
import io.sellmair.okay.utils.log

suspend fun OkContext.kotlinCompile(): OkPath {
    println("kotlinCompile called")
    val kotlinSources = modulePath("src").walk().withExtension("kt")

    /*
    println("Waiting for dependencies")
    val dependencies = mavenResolveCompileDependencies() +
            kotlinCompileDependencies()


     */


    return kotlinCompile(kotlinSources, emptyList(), modulePath("build/classes"))
}

suspend fun OkContext.kotlinCompile(
    sources: OkFileCollection,
    dependencies: List<OkPath>,
    outputDirectory: OkPath
): OkPath {

    println("kotlinCompile2  called")
    return cachedCoroutine(
        describeCoroutine("kotlinCompile", verbosity = Info),
        input = sources.asInput() +
                dependencies.map { it.asInput() }.asInput(),
        output = OkOutputDirectory(outputDirectory),
        serializer = OkPath.serializer()
    ) {
        log("Compiling Kotlin")
        KotlinCompileJvmRequest(
            moduleName = moduleName(),
            sources = sources.resolve(ctx),
            dependencies = dependencies,
            outputDirectory = outputDirectory
        ).execute<KotlinCompileJvmResponse>().outputDirectory
    }
}
