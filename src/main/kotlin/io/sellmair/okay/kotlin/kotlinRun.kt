package io.sellmair.okay.kotlin

import io.sellmair.okay.OkContext
import io.sellmair.okay.maven.mavenResolveDependencies
import io.sellmair.okay.modulePath
import java.net.URLClassLoader
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

suspend fun OkContext.kotlinRun(target: String? = null, arguments: List<String>) {
    val mavenDependencies = mavenResolveDependencies()
    val moduleDependencies = kotlinCompileDependencies()
    val compiled = kotlinCompile().await()


    val runPropertiesFile = modulePath("okay.run${if (target != null) ".${target}" else ""}.properties").system()
    if (!runPropertiesFile.isRegularFile()) error("Missing '$runPropertiesFile'")
    val properties = runPropertiesFile.inputStream().buffered().use { stream ->
        Properties().apply { load(stream) }
    }

    val mainClass = properties["mainClass"]?.toString() ?: error("Missing 'mainClass' property")

    val loader = URLClassLoader.newInstance(
        mavenDependencies.await().map { it.system().toUri().toURL() }.toTypedArray() +
                moduleDependencies.await().map { it.system().toUri().toURL() } +
                compiled.system().toUri().toURL()
    )

    println(
        """
        | ___________________________________
        | ___________________________________ 
        |  _____             
        | |  __ \            
        | | |__) |   _ _ __  
        | |  _  / | | | '_ \ 
        | | | \ \ |_| | | | |
        | |_|  \_\__,_|_| |_|
        | mainClass: $mainClass
        | ___________________________________
        | ___________________________________
        |        
        """.trimMargin()
    )

    val loadedMainClass = loader.loadClass(mainClass)
    try {
        loadedMainClass.getMethod("main").invoke(null)
    } catch (t: NoSuchMethodException) {
        loadedMainClass.getMethod("main", Array<String>::class.java).invoke(null, arguments.toTypedArray())
    }
}
