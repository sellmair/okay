import kotlin.io.path.Path
import kotlin.io.path.writeText

fun other(args: Array<String>) {
    Path(args.single()).writeText("other")
}