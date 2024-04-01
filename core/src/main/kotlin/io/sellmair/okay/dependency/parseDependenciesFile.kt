package io.sellmair.okay.dependency

import io.sellmair.okay.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@OptIn(ExperimentalSerializationApi::class)
suspend fun OkContext.parseDependenciesFile(): OkDependenciesFile? {
    val dependenciesFile = modulePath("okay.libs.json")

    return memoizedCoroutine(
        describeCoroutine("parseLibsFile"),
        input = OkInputFile(dependenciesFile)
    ) task@{
        val file = dependenciesFile.system()
        if (!file.exists()) return@task null
        val parsedObject = file.inputStream().buffered().use { inputStream ->
            Json.decodeFromStream<JsonElement>(inputStream)
        }.jsonObject

        val declarations = parsedObject.keys.map { dependencyKey ->
            OkDependencyDeclaration(
                value = dependencyKey,
                isExported = parsedObject.getValue(dependencyKey).isExported(),
                isRuntime = parsedObject.getValue(dependencyKey).isRuntime(),
                isCompile = parsedObject.getValue(dependencyKey).isCompile()
            )
        }

        OkDependenciesFile(modulePath(), declarations)
    }
}

private fun JsonElement.isExported(): Boolean {
    if (this is JsonPrimitive) {
        return this.boolean
    }

    if (this is JsonObject) {
        return get("isExported")?.jsonPrimitive?.boolean ?: false
    }

    return false
}

private fun JsonElement.isRuntime(): Boolean {
    if (this is JsonObject) {
        return get("isRuntime")?.jsonPrimitive?.boolean ?: true
    }

    return true
}

private fun JsonElement.isCompile(): Boolean {
    if (this is JsonObject) {
        return get("isCompile")?.jsonPrimitive?.boolean ?: true
    }

    return true
}


