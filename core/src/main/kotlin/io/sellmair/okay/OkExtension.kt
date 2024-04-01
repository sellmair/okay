package io.sellmair.okay

import java.util.*

interface OkExtension

internal inline fun <reified T : OkExtension> okExtensions(): List<T> {
    return ServiceLoader.load(T::class.java).toList()
}

internal inline fun <reified T : OkExtension> okExtensions(action: (T) -> Unit) {
    return okExtensions<T>().forEach(action)
}