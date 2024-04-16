package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.clean.okClean
import io.sellmair.okay.kotlin.kotlinPackage
import io.sellmair.okay.kotlin.kotlinRun
import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.assertContainsLog
import io.sellmair.okay.utils.runOkTest
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
class SampleProjectMultiModuleTest {

    @TempDir
    lateinit var tempDir: Path

    lateinit var projectDir: Path

    @BeforeTest
    fun setup() {
        projectDir = tempDir.resolve("multiModule")
        projectDir = Path("samples/multiModule").copyToRecursively(projectDir, overwrite = true, followLinks = false)

        Path("samples/multiModule").copyToRecursively(projectDir, overwrite = true, followLinks = false)
        runOkTest(OkRoot(projectDir)) { okClean() }
    }

    @Test
    fun `test - run`() = runOkTest(OkRoot(projectDir)) {
        kotlinRun()
        assertContainsLog("modules/library", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("modules/utils", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
    }

    @Test
    fun `test - package`() = runOkTest(OkRoot(projectDir)) {
        kotlinPackage()
        assertContainsLog("modules/library", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("modules/utils", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog(
            "<root>", "kotlinPackage", "Packaged Application in '${ansiGreen}build/application$ansiReset'"
        )

        if (!projectDir.resolve("build/application/multiModule.jar").isRegularFile()) {
            fail("Missing 'multiModule.jar' file in application")
        }
    }


}