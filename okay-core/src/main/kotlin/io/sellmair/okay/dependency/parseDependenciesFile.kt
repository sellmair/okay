package io.sellmair.okay.dependency

import io.sellmair.okay.OkContext
import io.sellmair.okay.describeCoroutine
import io.sellmair.okay.fs.isRegularFile
import io.sellmair.okay.fs.readText
import io.sellmair.okay.input.asInput
import io.sellmair.okay.memoizedCoroutine
import io.sellmair.okay.modulePath
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
suspend fun OkContext.parseDependenciesFile(): OkDependenciesFile? {
    val dependenciesFile = modulePath("okay.libs.json")

    return memoizedCoroutine(
        describeCoroutine("parseLibsFile"),
        input = dependenciesFile.asInput()
    ) task@{
        if (!dependenciesFile.isRegularFile()) return@task null
        val parsedObject =Json.decodeFromString<JsonElement>(dependenciesFile.readText())
        .jsonObject

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


