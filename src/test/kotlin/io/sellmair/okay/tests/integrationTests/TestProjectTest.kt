package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.clean.okClean
import io.sellmair.okay.kotlin.kotlinPackage
import io.sellmair.okay.kotlin.kotlinRun
import io.sellmair.okay.utils.*
import io.sellmair.okay.utils.ansiGreen
import io.sellmair.okay.utils.ansiReset
import io.sellmair.okay.utils.assertContainsLog
import io.sellmair.okay.utils.clearLogs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile


class TestProjectTest {

    @Test
    fun `test - run`() = runOkTest(OkRoot(Path("testProject"))) {
        okClean()
        clearLogs()

        kotlinRun()
        assertContainsLog("modules/library", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("modules/utils", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
        clearLogs()
    }

    @Test
    fun `test - package`() = runOkTest(OkRoot(Path("testProject"))) {
        okClean()
        clearLogs()

        kotlinPackage()
        assertContainsLog("modules/library", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("modules/utils", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog(
            "<root>", "kotlinPackage", "Packaged Application in '${ansiGreen}build/application$ansiReset'"
        )

        if (!Path("testProject/build/application/testProject.jar").isRegularFile()) {
            fail("Missing 'testProject.jar' file in application")
        }
    }


}