import kotlin.io.path.Path
import kotlin.io.path.writeText

fun main(args: Array<String>) {
    Path(args.single()).writeText("main")
}