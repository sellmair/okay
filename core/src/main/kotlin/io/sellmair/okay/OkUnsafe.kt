package io.sellmair.okay

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
annotation class OkUnsafe(@Suppress("unused") val message: String = "")