package io.sellmair.okay.zip

import io.sellmair.okay.*
import io.sellmair.okay.io.OkPath
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createParentDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream


suspend fun OkContext.zipFiles(
    zipFile: OkPath,
    files: Map<String, OkPath> = emptyMap(),
    data: Map<String, ByteArray> = emptyMap(),
): OkPath {
    /* Hash that will change if the layout of the files or the input byteArrays would change */
    val layoutInputHash = hash {
        files.forEach { (name, path) ->
            push(name)
            push(path)
        }

        data.forEach { (name, value) ->
            push(name)
            push(value)
        }
    }

    return cachedCoroutine(
        descriptor = describeCoroutine("zip", verbosity = OkCoroutineDescriptor.Verbosity.Debug),
        input = OkInputs(files.values.map { OkInputFile(it) }) + OkHashInput(layoutInputHash),
        output = OkOutputFile(zipFile)
    ) {
        zipFile.system().createParentDirectories()
        ZipOutputStream(zipFile.system().outputStream().buffered()).use { out ->
            files.forEach { (name, file) ->
                val sanitizedName = if (file.system().isDirectory() && !name.endsWith("/")) name + "/"
                else name

                out.putNextEntry(ZipEntry(sanitizedName))
                Files.copy(file.system(), out)
                out.closeEntry()
            }

            data.forEach { (name, value) ->
                out.putNextEntry(ZipEntry(name))
                out.write(value)
                out.closeEntry()
            }
        }
        zipFile
    }
}
