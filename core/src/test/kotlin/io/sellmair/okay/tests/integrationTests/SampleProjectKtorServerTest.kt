package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.clean.okClean
import io.sellmair.okay.kotlin.kotlinPackage
import io.sellmair.okay.kotlin.kotlinRun
import io.sellmair.okay.utils.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.copyToRecursively
import kotlin.io.path.isRegularFile
import kotlin.test.BeforeTest


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
    @Disabled
    fun `test - run`() = runOkTest(OkRoot(projectDir)) {
        kotlinRun()
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog(
            "<root>", "kotlinPackage", "Packaged Application in '${ansiGreen}build/application$ansiReset'"
        )

        if (!projectDir.resolve("build/application/ktorServer.jar").isRegularFile()) {
            fail("Missing 'ktorServer.jar' file in application")
        }
    }
}