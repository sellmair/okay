package io.sellmair.okay

interface OkContext {
    val stack: List<String>

    fun log(value: String)

    fun <T> launchTask(
        title: String, input: OkInput, output: OkOutput, body: suspend OkContext.() -> T
    ): OkAsync<T>

    suspend fun <T> await0(async: OkAsync<T>): T

    suspend fun <T> OkAsync<T>.await(): T = await0(this@await)
}


fun <T> OkContext.launchTask(
    title: String,
    input: List<OkInput>,
    output: List<OkOutput>,
    body: suspend OkContext.() -> T
): OkAsync<T> {
    return launchTask(title, OkCompositeInput(input), OkCompositeOutput(output), body)
}
