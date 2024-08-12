package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.fraunhofer.iem.kpiCalculator.adapter.AdapterResult
import de.fraunhofer.iem.kpiCalculator.adapter.tools.SupportedTool
import de.fraunhofer.iem.kpiCalculator.adapter.tools.occmd.OccmdAdapter
import de.fraunhofer.iem.kpiCalculator.model.kpi.hierarchy.KpiCalculationResult
import de.fraunhofer.iem.spha.cli.SphaToolCommandBase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.outputStream
import kotlin.io.path.writer

class TransformToolResultCommand : SphaToolCommandBase(name = "transform",
    help = "transforms a specified KPI-provider (such as a SAST tool) result into a uniform data format, " +
            "so that it can be used for the 'calculate' command.") {

    private val toolName by option("-t", "--tool",
        help = "The identifier of the KPI-provider tool that produced the input. " +
                "Use the command --list-tools to get a list of available identifiers.")
    .required()

    private val output by option("-o", "--output",
        help = "The output directory where the result of the operation is stored. Default is the current working directory.")

    private val input by option("-i", "--input",
        help = "The input data to transform.")
    //.required()

    //private val options by requireObject<CommonOptions>()

    override fun run() {
        super.run()

        val tool = SupportedTool.fromName(toolName)
        val result : Collection<AdapterResult> = when (tool){
            SupportedTool.Occmd -> {
//                val adapterInput = OccmdAdapter.createInputFrom(input)
//                OccmdAdapter.transformDataToKpi(adapterInput)
                throw NotImplementedError()
            }
        }

        createResultFile().outputStream().use {
            Json.encodeToStream(result, it)
        }
    }

    private fun createResultFile(): Path {
        val fileName = "$toolName-result.json"
        // Use current working directory if input is null.
        val outputPath = Paths.get(input ?: "", fileName).toAbsolutePath()
        return Files.createFile(outputPath)
    }
}
