@file:OptIn(ExperimentalCoroutinesApi::class)

package io.sellmair.okay.maven

import io.sellmair.okay.*
import io.sellmair.okay.input.asInput
import io.sellmair.okay.output.OkOutput
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private val downloadDispatcher = Dispatchers.Default.limitedParallelism(16)

internal suspend fun OkContext.mavenResolveDependencyTree(
    declaredDependencies: List<MavenCoordinates>,
    scope: MavenResolveDependenciesScope
): List<MavenCoordinates> {
    return cachedCoroutine(
        describeRootCoroutine("mavenResolveDependencyTree"),
        input = declaredDependencies.map {
            hash { push(it.toString()) }.asInput()
        }.asInput(),
        output = OkOutput.none()
    ) {
        withOkContext(downloadDispatcher) {
            val results = declaredDependencies.toMutableSet()
            var queue = declaredDependencies

            while (queue.isNotEmpty()) {
                val dependencies = queue.map { coordinates ->
                    async {
                        mavenResolvePom(coordinates)?.dependencies.orEmpty()
                            .filter { it in scope }.map { it.coordinates }
                    }
                }.awaitAll().flatten()

                queue = dependencies.filter { coordinates ->
                    results.add(coordinates)
                }
            }

            results.toList()
        }
    }
}