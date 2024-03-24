package io.sellmair.okay

interface OkContext {

    fun log(value: String)

    fun <T> cached(
        title: String, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
    ): OkAsync<T>
}

fun <T> OkContext.cached(
    title: String,
    input: List<OkInput>,
    output: List<OkOutput>,
    body: suspend OkContext.() -> T
): OkAsync<T> {
    return cached(title, OkCompositeInput(input), OkCompositeOutput(output), body)
}
