package io.sellmair.okay.tests.unitTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.path
import io.sellmair.okay.utils.runOkTest
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class OkPathTest {
    @Test
    fun `test - conversion to system path`() = runOkTest {
        val okFoo = path("foo")
        assertEquals(Path("foo"), okFoo.system())
        assertEquals(okFoo, okFoo.system().ok())
    }

    @Test
    fun `test - conversion to system path - with custom root`() = runOkTest(OkRoot(Path("path/to/project"))) {
        val okFoo = path("foo")
        assertEquals(Path("path/to/project/foo"), okFoo.system())
        assertEquals(okFoo, okFoo.system().ok())
    }

    @Test
    fun `test - absolutePath`() = runOkTest(OkRoot(Path("path/to/project"))) {
        val okFoo = path("/my/absolute/path")
        assertEquals(Path("/my/absolute/path"), okFoo.system())
    }
}