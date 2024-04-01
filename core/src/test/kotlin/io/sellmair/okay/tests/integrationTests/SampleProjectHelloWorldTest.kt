@file:OptIn(ExperimentalPathApi::class)

package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.clean.okClean
import io.sellmair.okay.kotlin.kotlinRun
import io.sellmair.okay.utils.assertContainsLog
import io.sellmair.okay.utils.runOkTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.copyToRecursively
import kotlin.test.BeforeTest

class SampleProjectHelloWorldTest {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var projectDir: Path

    @BeforeTest
    fun setup() {
        projectDir = tempDir.resolve("helloWorld")
        projectDir = Path("samples/helloWorld").copyToRecursively(projectDir, overwrite = true, followLinks = false)
        runOkTest(OkRoot(projectDir)) { okClean() }
    }

    @Test
    fun `test - run`() = runOkTest(OkRoot(projectDir)) {
        kotlinRun()
        assertContainsLog("<root>", "kotlinCompile", "Compiling Kotlin")
        assertContainsLog("<root>", "kotlinRun", "run: MainKt.main()")
    }
}
