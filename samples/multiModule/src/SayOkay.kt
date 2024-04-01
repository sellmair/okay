import kotlinx.coroutines.runBlocking
import io.sellmair.testProject.library.Library

fun main() {
    runBlocking {
        println("Okay: ${Library().importantFunction()}")
    }
}