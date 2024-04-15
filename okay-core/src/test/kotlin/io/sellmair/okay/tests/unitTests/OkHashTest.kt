package io.sellmair.okay.tests.unitTests

import io.sellmair.okay.input.OkInput
import io.sellmair.okay.output.OkOutput
import io.sellmair.okay.utils.runOkTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OkHashTest {

    @Test
    fun `test - OkInput none`() = runOkTest {
        assertEquals(OkInput.none().currentHash(ctx), OkInput.none().currentHash(ctx))
    }

    @Test
    fun `test - OkOutput none`() = runOkTest {
        assertEquals(OkOutput.none().currentHash(ctx), OkOutput.none().currentHash(ctx))
    }
}