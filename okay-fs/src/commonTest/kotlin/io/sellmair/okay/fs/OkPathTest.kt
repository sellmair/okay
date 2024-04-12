package io.sellmair.okay.fs

import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OkPathTest {

    @Test
    fun `test - isRegularFile`() {
        val fs = OkFsImpl("my/workingDir".toPath(), FakeFileSystem())
        val path = fs.path("foo")

        // File does not exist at all!
        assertFalse(path.isRegularFile(), "File is not present!")

        // Create as directory
        path.createDirectories()
        assertFalse(path.isRegularFile(), "File is a directory")
        assertTrue(path.isDirectory(), "File is a directory")

        // Delete and create as file
        path.delete()
        path.write("hello".encodeToByteArray())
        assertTrue(path.isRegularFile(), "File is regular file")

        // Delete again
        path.delete()
        assertFalse(path.isRegularFile(), "File was deleted")
    }

    @Test
    fun `test - serialization`() {
        val fs = OkFs("/my/working/dir")
        val path = fs.path("my/path")

        val format = Json { prettyPrint = true }

        val json = format.encodeToString(OkPath.serializer(), path)
        assertEquals(
            """
                {
                    "root": "/my/working/dir",
                    "path": "my/path"
                }""".trimIndent(),
            json
        )
        
        val decoded = format.decodeFromString<OkPath>(json)
        assertEquals(path, decoded)
    }
}