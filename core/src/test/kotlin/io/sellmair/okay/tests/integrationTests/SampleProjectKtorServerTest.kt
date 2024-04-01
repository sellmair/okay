package io.sellmair.okay.tests.integrationTests

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.sellmair.okay.OkRoot
import io.sellmair.okay.clean.okClean
import io.sellmair.okay.kotlin.kotlinPackage
import io.sellmair.okay.kotlin.kotlinRun
import io.sellmair.okay.utils.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.copyToRecursively
import kotlin.io.path.isRegularFile
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds


@ExperimentalPathApi
class SampleProjectKtorServerTest {

    @TempDir
    lateinit var tempDir: Path

    lateinit var projectDir: Path

    @BeforeTest
    fun setup() {
        projectDir = tempDir.resolve("ktorServer")
        projectDir = Path("samples/ktorServer").copyToRecursively(projectDir, overwrite = true, followLinks = false)

        Path("samples/ktorServer").copyToRecursively(projectDir, overwrite = true, followLinks = false)
        runOkTest(OkRoot(projectDir)) { okClean() }
    }


    @Test
    fun `test - package`() = runOkTest(OkRoot(projectDir)) {
        kotlinPackage()
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog(
            "<root>", "kotlinPackage", "Packaged Application in '${ansiGreen}build/application$ansiReset'"
        )

        if (!projectDir.resolve("build/application/ktorServer.jar").isRegularFile()) {
            fail("Missing 'ktorServer.jar' file in application")
        }
    }

    @Test
    fun `test - run`() {
        runOkTest(OkRoot(projectDir)) {
            val runThread = kotlinRun()
            runThread.setUncaughtExceptionHandler { _, e -> }
            assertContainsLog("<root>", "kotlinRun", "run: com.sample.MainKt.main()")
            delay(1.seconds)
            assertEquals("Hello, world!", HttpClient().get("http://0.0.0.0:8080").bodyAsText())

            runThread.interrupt()
        }
    }
}