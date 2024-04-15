package io.sellmair.okay.zip

import io.sellmair.okay.*
import io.sellmair.okay.fs.*
import io.sellmair.okay.input.OkInputFile
import io.sellmair.okay.input.asInput
import io.sellmair.okay.input.plus
import io.sellmair.okay.output.OkOutputFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


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
        input = files.values.map { OkInputFile(it) }.asInput() + layoutInputHash.asInput(),
        output = OkOutputFile(zipFile)
    ) {
        zipFile.createParentDirectories()

        ZipOutputStream(zipFile.sink().outputStream().buffered()).use { out ->
            files.forEach { (name, file) ->
                val sanitizedName = if (file.isDirectory() && !name.endsWith("/")) name + "/"
                else name

                out.putNextEntry(ZipEntry(sanitizedName))
                if (file.isRegularFile()) {
                    file.source { source -> source.inputStream().copyTo(out) }
                }
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
