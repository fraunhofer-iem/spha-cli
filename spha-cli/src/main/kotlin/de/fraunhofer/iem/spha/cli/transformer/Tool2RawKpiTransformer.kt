package de.fraunhofer.iem.spha.cli.transformer

import de.fraunhofer.iem.kpiCalculator.adapter.AdapterResult
import de.fraunhofer.iem.kpiCalculator.adapter.tools.trivy.TrivyAdapter
import de.fraunhofer.iem.kpiCalculator.model.adapter.trivy.TrivyDto
import de.fraunhofer.iem.kpiCalculator.model.kpi.RawValueKpi
import de.fraunhofer.iem.spha.cli.StrictModeConstraintFailed
import de.fraunhofer.iem.spha.cli.util.InputHelper.getSingleInputStreamFromInputFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.FileSystem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal fun interface RawKpiTransformer {
    fun getRawKpis(options: TransformerOptions, strictMode: Boolean): Collection<RawValueKpi>
}

class ToolNotFoundException(message: String) : Exception(message)

internal class Tool2RawKpiTransformer : RawKpiTransformer, KoinComponent {

    private val _fileSystem by inject<FileSystem>()
    private val _logger = KotlinLogging.logger {}

    override fun getRawKpis(
        options: TransformerOptions,
        strictMode: Boolean,
    ): Collection<RawValueKpi> {

        val result: Collection<AdapterResult> =
            when (options.tool) {
                //            "occmd" -> {
                //                val adapterInput: OccmdDto = OccmdAdapter.createInputFrom(input)
                //                OccmdAdapter.transformDataToKpi(adapterInput)
                //            }
                "trivy" -> {
                    getSingleInputStreamFromInputFile(_fileSystem, options.inputFiles, strictMode)
                        .use {
                            _logger.info { "Selected supported tool: Trivy" }
                            val adapterInput: TrivyDto = TrivyAdapter.dtoFromJson(it)
                            return@use TrivyAdapter.transformDataToKpi(listOf(adapterInput))
                        }
                }

                else -> throw ToolNotFoundException("Tool ${options.tool} is not yet supported.")
            }

        val rawKpis =
            result.mapNotNull {
                if (it !is AdapterResult.Success) return@mapNotNull null
                return@mapNotNull it.rawValueKpi
            }

        // If we have unequal counts, we know that adapter returned faulted elements. Thus, we throw
        // in strict mode.
        if (strictMode && rawKpis.count() != result.count()) {
            throw StrictModeConstraintFailed("The adapter produced faulted results.")
        }

        return rawKpis
    }
}
