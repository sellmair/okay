package io.sellmair.okay.fs

import okio.FileSystem
import platform.posix.chmod

actual fun FileSystem.setIsExecutable(path: okio.Path, isExecutable: Boolean) {
    // TODO
}