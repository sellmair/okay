import kotlinx.coroutines.runBlocking
import io.sellmair.testProject.library.Library

fun main() {
    runBlocking {
        Library().importantFunction()

        println("Okay!")
    }
}