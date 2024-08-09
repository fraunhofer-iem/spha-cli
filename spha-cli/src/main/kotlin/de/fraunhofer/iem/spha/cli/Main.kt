package de.fraunhofer.iem.spha.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import de.fraunhofer.iem.spha.cli.commands.TransformResultCommand
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

private class SphaToolCommands : CliktCommand(){
    override fun run() = Unit
}

