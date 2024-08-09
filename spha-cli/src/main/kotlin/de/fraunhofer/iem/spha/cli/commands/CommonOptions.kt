package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class CommonOptions: OptionGroup("Common Options:") {
    val verbose by option("--verbose", "-v",
        help="When set, the application provides detailed logging. Default is unset.")
        .flag()

    val strict by option("--strict",
        help="When set, the application is less tolerant to unknown input formats. Default is unset.")
        .flag()
}