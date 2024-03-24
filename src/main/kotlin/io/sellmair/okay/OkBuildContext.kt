package io.sellmair.okay

interface OkBuildContext {

    fun log(value: String)

    fun <T> cached(
        title: String, input: OkInput, output: OkOutput, body: suspend OkBuildContext.() -> T
    ): OkAsync<T>
}

fun <T> OkBuildContext.cached(
    title: String,
    input: List<OkInput>,
    output: List<OkOutput>,
    body: suspend OkBuildContext.() -> T
): OkAsync<T> {
    return cached(title, OkCompositeInput(input), OkCompositeOutput(output), body)
}
