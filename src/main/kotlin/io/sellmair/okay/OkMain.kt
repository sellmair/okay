@file:JvmName("OkMain")

package io.sellmair.okay

import io.sellmair.okay.kotlin.kotlinCompile
import io.sellmair.okay.utils.log
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively

@OptIn(ExperimentalPathApi::class)
fun main(args: Array<String>) {

    println(
        """
        |   ___   _  __    _  __   __
        |  / _ \ | |/ /   / \ \ \ / /
        | | | | || ' /   / _ \ \ V / 
        | | |_| || . \  / ___ \ | |  
        |  \___/ |_|\_\/_/   \_\|_|  
    """.trimMargin()
    )

    ok {
        if (args.singleOrNull() == "build") {
            kotlinCompile().await()

        }

        if (args.singleOrNull() == "clean") {
            log("Cleaning .okay")
            Path(".okay").deleteRecursively()

            log("Cleaning build")
            Path("build").deleteRecursively()
        }
    }
}
