package io.sellmair.okay.kotlin

import io.sellmair.okay.*
import io.sellmair.okay.OkCoroutineDescriptor.Verbosity.Info
import io.sellmair.okay.fs.absolutePathString
import io.sellmair.okay.fs.isRegularFile
import io.sellmair.okay.fs.readText
import io.sellmair.okay.input.OkInputFile
import io.sellmair.okay.maven.mavenResolveRuntimeDependencies
import io.sellmair.okay.utils.log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.net.URLClassLoader
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.Path


suspend fun OkContext.kotlinRun(target: String? = null, arguments: List<String> = emptyList()): Thread = withOkStack(
    describeCoroutine<Unit>("kotlinRun", verbosity = Info)
) {
    val mavenDependencies = async { mavenResolveRuntimeDependencies() }
    val moduleDependencies = async { kotlinCompileRuntimeDependencies() }
    val compiled = async { kotlinCompile() }

    val options = parseKotlinRunOptions(target)
    val className = options?.className ?: "MainKt"
    val functionName = options?.functionName ?: "main"


    val loader = URLClassLoader.newInstance(
        mavenDependencies.await().map { Path(it.absolutePathString()).toUri().toURL() }.toTypedArray() +
                moduleDependencies.await().map { Path(it.absolutePathString()).toUri().toURL() } +
                Path(compiled.await().absolutePathString()).toUri().toURL()
    )

    log("run: $className.$functionName()")

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

    thread(name = "Main", contextClassLoader = loader) {
        val loadedMainClass = loader.loadClass(className)
        try {
            loadedMainClass.getMethod(functionName).invoke(null)
        } catch (t: NoSuchMethodException) {
            loadedMainClass.getMethod(functionName, Array<String>::class.java).invoke(null, arguments.toTypedArray())
        }
    }
}

internal class KotlinRunOptions(
    val className: String,
    val functionName: String?
)

@OptIn(ExperimentalSerializationApi::class)
internal suspend fun OkContext.parseKotlinRunOptions(target: String? = null): KotlinRunOptions? {
    val runConfigurationFile = modulePath("okay.run.json")

    return memoizedCoroutine(
        describeCoroutine("parseKotlinRunOptions"),
        input = OkInputFile(runConfigurationFile)
    ) coroutine@{
        if (!runConfigurationFile.isRegularFile()) {
            return@coroutine null
        }

        val parsed = Json.decodeFromString<JsonElement>(runConfigurationFile.readText())


        if (target == null && parsed is JsonObject) {
            return@coroutine parsed.toKotlinRunOptions()
        }

        val jsonObjectForRunTarget = parsed.jsonArray.filterIsInstance<JsonObject>()
            .find { jsonObject -> (jsonObject["name"] as? JsonPrimitive)?.contentOrNull == target }
            ?: error("Missing run target '$target' in $runConfigurationFile")

        jsonObjectForRunTarget.toKotlinRunOptions()
    }
}

private fun JsonObject.toKotlinRunOptions(): KotlinRunOptions {
    return KotlinRunOptions(
        className = this["class"]?.jsonPrimitive?.content ?: error("Missing \"class\" in $this"),
        functionName = this["function"]?.jsonPrimitive?.contentOrNull
    )
}