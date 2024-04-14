package io.sellmair.okay.tests.unitTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.fs.absolutePathString
import io.sellmair.okay.moduleName
import io.sellmair.okay.path
import io.sellmair.okay.utils.runOkTest
import io.sellmair.okay.withOkModule
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.test.assertEquals

class OkPathTest {
    @Test
    fun `test - conversion to system path`() = runOkTest {
        val okFoo = path("foo")
        assertEquals(Path("foo").absolutePathString(), okFoo.absolutePathString())
    }

    @Test
    fun `test - moduleName`() {
        runOkTest {
            assertEquals("okay", moduleName())
            assertEquals(File(".").canonicalFile.name, moduleName())
        }
    }

    @Test
    fun `test - moduleName - nested`() {
        runOkTest(OkRoot(Path("some/root"))) {
            assertEquals("root", moduleName())
            withOkModule(path("myModule")) {
                assertEquals("myModule", moduleName())
            }
        }
    }
}