package io.sellmair.okay.kotlin

import io.sellmair.okay.OkContext
import io.sellmair.okay.async
import io.sellmair.okay.maven.mavenResolveRuntimeDependencies
import io.sellmair.okay.modulePath
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.jetbrains.kotlin.konan.file.use
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

fun OkContext.kotlinRun(target: String? = null, arguments: List<String>) = async{
    val mavenDependencies = mavenResolveRuntimeDependencies()
    val moduleDependencies = kotlinCompileRuntimeDependencies()
    val compiled = kotlinCompile().await()

    val runConfigurationFile = modulePath("okay.run.json").system()
    if (!runConfigurationFile.isRegularFile()) error("Missing '$runConfigurationFile'")
    val options = parseKotlinRunOptions(runConfigurationFile, target)
    val className = options.className
    val functionName = options.functionName ?: "main"


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
        | ${if (target != null) "target: '$target'" else ""}
        | class: '$className'
        | function: '$functionName' 
        | ___________________________________
        | ___________________________________
        |        
        """.trimMargin()
    )

    val loadedMainClass = loader.loadClass(className)
    try {
        loadedMainClass.getMethod(functionName).invoke(null)
    } catch (t: NoSuchMethodException) {
        loadedMainClass.getMethod(functionName, Array<String>::class.java).invoke(null, arguments.toTypedArray())
    }
}

private class KotlinRunOptions(
    val className: String,
    val functionName: String?
)

@OptIn(ExperimentalSerializationApi::class)
private fun parseKotlinRunOptions(path: Path, target: String?): KotlinRunOptions {
    val parsed = path.inputStream().buffered().use { inputStream ->
        Json.decodeFromStream<JsonElement>(inputStream)
    }

    if (target == null && parsed is JsonObject) {
        return parsed.toKotlinRunOptions()
    }

    val jsonObjectForRunTarget = parsed.jsonArray.filterIsInstance<JsonObject>()
        .find { jsonObject -> (jsonObject["name"] as? JsonPrimitive)?.contentOrNull == target }
        ?: error("Missing run target '$target' in $path")

    return jsonObjectForRunTarget.toKotlinRunOptions()
}

private fun JsonObject.toKotlinRunOptions(): KotlinRunOptions {
    return KotlinRunOptions(
        className = this["class"]?.jsonPrimitive?.content ?: error("Missing \"class\" in $this"),
        functionName = this["function"]?.jsonPrimitive?.contentOrNull
    )
}