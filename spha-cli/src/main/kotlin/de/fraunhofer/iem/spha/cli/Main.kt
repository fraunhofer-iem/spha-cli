package de.fraunhofer.iem.spha.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger{}
    logger.debug { "Raw command line args: '${args.joinToString()}'" }

    try {
        SphaToolCommands()
            .subcommands(TransformResultCommand())
            .main(args)
    } catch (e : Exception){
        logger.error(e, {e.message})
        exitProcess(1)
    }
}

class CommonOptions: OptionGroup("Standard Options:") {
    val verbose by option("--verbose", "-v", help="When set, the application provides detailed logging").flag()
}


private class SphaToolCommands : CliktCommand(){
    override fun run() = Unit
}

class TransformResultCommand : CliktCommand(name = "transform") {
    private val commonOptions by CommonOptions()
    private val toolName by option("-t", "--tool")//.required()
    private val input by option("-i", "--input")//.required()
    private val output by option("-o", "--output")//.required()

    override fun run() {
        // TODO: 0. Move logic to new class
        //       1. Call adapter factory [lib] with inputs toolName and input file (not if deserialized here or in lib)
        //       2. Call adapter to get transformed model
        //       3. Deserialize model to output file
    }

    override fun commandHelp(context: Context): String {
        return "transforms a tool's result into a representation that can be used as an input to the <calculate> command"
    }
}