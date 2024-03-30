package io.sellmair.okay.tests.integrationTests

import io.sellmair.okay.OkRoot
import io.sellmair.okay.kotlin.kotlinCompile
import io.sellmair.okay.path
import io.sellmair.okay.rootPath
import io.sellmair.okay.utils.assertCacheHit
import io.sellmair.okay.utils.assertCacheMiss
import io.sellmair.okay.utils.runOkTest
import io.sellmair.okay.utils.testProjectPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.*

@ExperimentalPathApi
class CacheTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test - kotlinCompile`() {
        val fooKt = tempDir.resolve("src/Foo.kt")
        fooKt.createParentDirectories()
        fooKt.writeText("class Foo")

        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
            assertCacheMiss(rootPath(), "kotlinCompile")
        }

        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
            assertCacheHit(rootPath(), "kotlinCompile")
        }

        fooKt.writeText("class Bar")
        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
            assertCacheMiss(rootPath(), "kotlinCompile")
        }

        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
            assertCacheHit(rootPath(), "kotlinCompile")
        }
    }

    @Test
    fun `test - module dependencies`() {
        testProjectPath("threeModules").copyToRecursively(tempDir, overwrite = true, followLinks = false)

        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
        }

        val newFile = tempDir.resolve("moduleB/src/new.kt").apply {
            createParentDirectories()
            writeText("class New")
        }

        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
            assertCacheMiss(path("moduleB"), "kotlinCompile")
            assertCacheMiss(path("moduleA"), "kotlinCompile")

            /* Hit expected as moduleB is not exported in moduleA*/
            assertCacheHit(rootPath(), "kotlinCompile")
        }

        newFile.deleteIfExists()
        runOkTest(OkRoot(tempDir)) {
            kotlinCompile()
            assertCacheHit(path("moduleB"), "kotlinCompile")
            assertCacheHit(path("moduleA"), "kotlinCompile")
            assertCacheHit(rootPath(), "kotlinCompile")
        }
    }
}