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
        val resolvedLock = ReentrantLock()
        val resolved = mutableSetOf(*declaredDependencies.toTypedArray())

        fun CoroutineScope.launchResolve(coordinates: MavenCoordinates): Job = launch(downloadDispatcher) {
            val dependencies = mavenResolvePom(coordinates)?.dependencies.orEmpty()
                .filter { it in scope }
                .map { it.coordinates }

            resolvedLock.withLock {
                dependencies.forEach { dependency ->
                    if (resolved.add(dependency)) {
                        launchResolve(dependency)
                    }
                }
            }
        }

        coroutineScope {
            declaredDependencies.forEach { dependency ->
                launchResolve(dependency)
            }
        }

        resolved.toList()
    }
}