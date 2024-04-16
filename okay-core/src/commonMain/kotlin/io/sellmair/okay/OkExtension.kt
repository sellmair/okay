package io.sellmair.okay


interface OkExtension

internal inline fun <reified T : OkExtension> okExtensions(): List<T> {
    return emptyList() // TODO NOW!
}

internal inline fun <reified T : OkExtension> okExtensions(action: (T) -> Unit) {
    return okExtensions<T>().forEach(action)
}