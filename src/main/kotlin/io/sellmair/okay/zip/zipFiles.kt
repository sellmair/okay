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
    files: Map<String, OkPath>
): OkPath {
    return cachedCoroutine(
        descriptor = describeCoroutine("zip", verbosity = OkCoroutineDescriptor.Verbosity.Debug),
        input = OkCompositeInput(files.values.map { OkFileInput(it) }),
        output = OkOutputFile(zipFile)
    ) {
        zipFile.system().createParentDirectories()
        ZipOutputStream(zipFile.system().outputStream().buffered()).use { out ->
            files.forEach { (name, file) ->
                val sanitizedName = if (file.system().isDirectory() && !name.endsWith("/")) name + "/"
                else name

                out.putNextEntry(ZipEntry(sanitizedName))
                Files.copy(file.system(), out)
            }
        }
        zipFile
    }
}
