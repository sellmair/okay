package io.sellmair.okay.fs

import okio.FileSystem
import okio.Path
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.getPosixFilePermissions
import kotlin.io.path.setPosixFilePermissions

actual fun FileSystem.setIsExecutable(path: Path, isExecutable: Boolean) {
    runCatching {
        path.toNioPath().setPosixFilePermissions(
            if (isExecutable) path.toNioPath().getPosixFilePermissions() + PosixFilePermission.OWNER_EXECUTE
            else path.toNioPath().getPosixFilePermissions() - PosixFilePermission.OWNER_EXECUTE
        )
    }
}