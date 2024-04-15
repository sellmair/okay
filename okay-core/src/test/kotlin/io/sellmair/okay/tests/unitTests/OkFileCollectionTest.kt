package io.sellmair.okay.tests.unitTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.fs.absolutePathString
import io.sellmair.okay.io.walk
import io.sellmair.okay.io.withExtension
import io.sellmair.okay.path
import io.sellmair.okay.utils.runOkTest
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class OkFileCollectionTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test - filtered file collection`() {
        val aKt = tempDir.resolve("foo/bar/a.kt")
        val bKt = tempDir.resolve("foo/bar/b.kt")
        val cKt = tempDir.resolve("foo/c.kt")
        val dKt = tempDir.resolve("d.kt")

        val aTxt = tempDir.resolve("foo/bar/a.txt")
        val bTxt = tempDir.resolve("b.txt")

        tempDir.resolve("foo/bar").createDirectories()
        aKt.writeText("class A")
        bKt.writeText("class B")
        cKt.writeText("class C")
        dKt.writeText("class D")
        aTxt.writeText("a")
        bTxt.writeText("b")

        runOkTest(OkRoot(tempDir)) {
            val resolved = path("").walk().withExtension("kt").resolve(ctx).toList()
            assertEquals(
                listOf(dKt, aKt, bKt, cKt).map { it.absolutePathString() }, resolved.map { it.absolutePathString() }
            )
        }

    }
}