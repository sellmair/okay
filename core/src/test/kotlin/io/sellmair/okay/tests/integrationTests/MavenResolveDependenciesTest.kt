@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.async
import io.sellmair.okay.io.walk
import io.sellmair.okay.maven.mavenResolveCompileDependencies
import io.sellmair.okay.maven.mavenResolveRuntimeDependencies
import io.sellmair.okay.path
import io.sellmair.okay.utils.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists
import kotlin.test.BeforeTest

class MavenResolveDependenciesTest {

    @TempDir
    lateinit var projectDir: Path

    @BeforeTest
    fun setup() {
        testProjectPath("simpleMavenDependency").copyToRecursively(projectDir, overwrite = true, followLinks = false)
    }

    @Test
    fun `test - simpleMavenDependency - downloads coroutines POM just once`() {
        runOkTest(OkRoot(projectDir)) {
            val runtime = async { mavenResolveRuntimeDependencies() }
            val compile = async { mavenResolveCompileDependencies() }
            runtime.await()
            compile.await()
            assertNoDuplicateLogs()
        }
    }

    @Test
    fun `test - simpleMavenDependency - downloads coroutines jar`() {
        runOkTest(OkRoot(projectDir)) {
            mavenResolveCompileDependencies()
            val expectedCoroutinesJar = path(".okay/libs/maven/org.jetbrains.kotlinx-kotlinx-coroutines-core-1.7.3.jar")
            if (!expectedCoroutinesJar.system().exists()) {
                val actualLibraries = path(".okay/libs/maven").walk().resolve(ctx).joinToString("\n")
                fail("Missing 'kotlinx-coroutines-core-jvm.jar'. Found:\n$actualLibraries")
            }
        }
    }
}