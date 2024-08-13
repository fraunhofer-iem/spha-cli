package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.fraunhofer.iem.kpiCalculator.adapter.tools.SupportedTool
import de.fraunhofer.iem.spha.cli.SphaToolCommandBase
import de.fraunhofer.iem.spha.cli.transformer.RawKpiTransformer
import de.fraunhofer.iem.spha.cli.transformer.TransformerOptions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.FileSystem
import java.nio.file.Path

internal class TransformToolResultCommand : SphaToolCommandBase(name = "transform",
    help = "transforms a specified KPI-provider (such as a SAST tool) result into a uniform data format, " +
            "so that it can be used for the 'calculate' command."), KoinComponent {

    private val toolName by option("-t", "--tool",
        help = "The identifier of the KPI-provider tool that produced the input. " +
                "Use the command --list-tools to get a list of available identifiers.")
    .required()

    private val output by option("-o", "--output",
        help = "The output directory where the result of the operation is stored. Default is the current working directory.")

    private val transformer by inject<RawKpiTransformer>()
    private val fileSystem by inject<FileSystem>()

    @OptIn(ExperimentalSerializationApi::class)
    override fun run() {
        super.run()

        val tool = SupportedTool.fromName(toolName)

        val result = transformer.getRawKpis(TransformerOptions(tool), strict)

        val resultPath = getResultFilePath()

        fileSystem.provider().newOutputStream(resultPath).use {
            Logger.trace{ "Storing result to '$resultPath'" }
            Json.encodeToStream(result, it)
        }
    }

    private fun getResultFilePath(): Path {
        val fileName = "$toolName-result.json"
        // Use current working directory if output is null.
        return fileSystem.getPath(output ?: "", fileName).toAbsolutePath()
    }
}
