package io.sellmair.okay.tests.unitTests

import io.sellmair.okay.*
import io.sellmair.okay.utils.runOkTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OkHashTest {

    @Test
    fun `test - OkInput none`() = runOkTest {
        assertEquals(OkInput.none().cacheKey(ctx), OkInput.none().cacheKey(ctx))
    }

    @Test
    fun `test - OkOutput none`() = runOkTest {
        assertEquals(OkOutput.none().cacheKey(), OkOutput.none().cacheKey())
    }
}