package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.fraunhofer.iem.kpiCalculator.adapter.tools.SupportedTool

class TransformResultCommand : CliktCommand(name = "transform",
    help = "transforms a specified KPI-provider (such as a SAST tool) result into a uniform data format, " +
            "so that it can be used for the 'calculate' command.") {
    private val commonOptions by CommonOptions()
    private val toolName by option("-t", "--tool",
        help = "The identifier of the KPI-provider tool that produced the input. " +
                "Use the command --list-tools to get a list of available identifiers.")
    .required()

    private val output by option("-o", "--output",
        help = "The output directory where the result of the operation is stored. Default is the current working directory.")
    //.required()

    private val input by option("-i", "--input",
        help = "The input data to transform.")
    //.required()

    override fun run() {

        // output/[toolName]-rawKip.json

        // Any possible input needs to exist as nullable variable
        val token : Any? = null
        val repo : Any? = null
        val input : Any? = null

        val tool = SupportedTool.fromName(toolName)
//        val result = when (tool){
//            SupportedTool.Trivy -> {
//                val adapter = TrivyAdapter()
//                val adapterInput = createInputFrom(input)
//                adapter.transformDataToKpi(adapterInput)
//            }
//            SupportedTool.Gitlab -> {
//                val adapter = GitlabAdapter()
//                val adapterInput = createInputFrom(client, token, repo)
//                adapter.transformDataToKpi(adapterInput)
//            }
//        }

        // TODO: 0. Move logic to new class
        //       1. Call adapter factory [lib] with inputs toolName and input file (not if deserialized here or in lib)
        //       2. Call adapter to get transformed model
        //       3. Deserialize model to output file
    }
}

