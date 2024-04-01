@file:JvmName("OkMain")

package io.sellmair.okay

import io.sellmair.okay.clean.okClean
import io.sellmair.okay.kotlin.kotlinCompile
import io.sellmair.okay.kotlin.kotlinJar
import io.sellmair.okay.kotlin.kotlinPackage
import io.sellmair.okay.kotlin.kotlinRun
import kotlin.io.path.ExperimentalPathApi

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
            kotlinCompile()
        }

        if (args.firstOrNull() == "run") {
            kotlinRun(args.getOrNull(1), args.drop(2)).join()
        }

        if (args.firstOrNull() == "jar") {
            kotlinJar()
        }

        if (args.firstOrNull() == "package" || args.firstOrNull() == "pkg") {
            kotlinPackage()
        }

        if (args.singleOrNull() == "clean") {
            okClean()
        }
    }
}

