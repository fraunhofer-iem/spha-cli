package de.fraunhofer.iem.spha.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import de.fraunhofer.iem.spha.cli.commands.TransformToolResultCommand
import de.fraunhofer.iem.spha.cli.transformer.RawKpiTransformer
import de.fraunhofer.iem.spha.cli.transformer.Tool2RawKpiTransformer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.slf4j.simple.SimpleLogger
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import kotlin.system.exitProcess

internal val appModules = module {
    single<RawKpiTransformer>{ Tool2RawKpiTransformer() }
    single<FileSystem>{ FileSystems.getDefault() }
}

fun main(args: Array<String>) {
    startKoin{
        modules(appModules)
    }

    try {
        MainSphaToolCommand()
            .subcommands(TransformToolResultCommand())
            .main(args)
    } catch (e : Exception){
        val logger = KotlinLogging.logger{}
        logger.error(e, {e.message})
        exitProcess(1)
    }
}

/**
 * The Main command of this application. Supports a global switch to enable verbose logging mode.
 */
private class MainSphaToolCommand : NoOpCliktCommand(){

    val verbose by option("--verbose", "-v",
        help="When set, the application provides detailed logging. Default is unset.")
        .flag()

    override fun run() {
        configureLogging()
    }

    private fun configureLogging(){
        if (verbose)
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    }
}

/**
 * Base class for all commands of this application, except for the main command.
 * @implNote Due to the design of clikt, the main command should be separate
 * and this base class should not introduce the --verbose switch. Otherwise, the following cli input would be legal:
 * './spha -v transform -v'. The first -v switch actually triggers the logging configuration,
 * where the second -v switch is independent to the first switch. This will cause confusion for users, which switch to use.
 */
internal abstract class SphaToolCommandBase(name: String? = null, help: String = "")
    : CliktCommand(name = name, help = help), KoinComponent {
    // NB: Needs to be lazy, as otherwise we initialize this variable before setting the logger configuration.
    private val _lazyLogger = lazy {  KotlinLogging.logger{} }
    protected val Logger get() = _lazyLogger.value

    val strict by option("--strict",
        help="When set, the application is less tolerant to unknown input formats. Default is unset.")
        .flag()

    override fun run() {
        Logger.trace { "Original command arguments: '${currentContext.originalArgv.joinToString()}}'" }
        Logger.debug { "Running command: $commandName" }
    }
}
