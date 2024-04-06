package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.kotlin.kotlinRun
import io.sellmair.okay.rootPath
import io.sellmair.okay.utils.assertCacheUpToDate
import io.sellmair.okay.utils.runOkTest
import io.sellmair.okay.utils.testProjectPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyToRecursively
import kotlin.io.path.readText
import kotlin.test.assertEquals

@OptIn(ExperimentalPathApi::class)
class RunTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test - run`() {
        testProjectPath("simpleRunnable").copyToRecursively(tempDir, overwrite = true, followLinks = false)
        val responseFile = tempDir.resolve("response.txt")
        runOkTest(OkRoot(tempDir)) {
            kotlinRun(arguments = listOf(responseFile.absolutePathString())).join()
            assertEquals("main", responseFile.readText())
        }

        runOkTest(OkRoot(tempDir)) {
            kotlinRun("other", listOf(responseFile.absolutePathString())).join()
            assertCacheUpToDate(rootPath(), "kotlinCompile")
            assertEquals("other", responseFile.readText())
        }
    }
}
