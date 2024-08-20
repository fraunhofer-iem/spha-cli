package de.fraunhofer.iem.spha.cli.transformer

import de.fraunhofer.iem.kpiCalculator.adapter.AdapterResult
import de.fraunhofer.iem.kpiCalculator.adapter.tools.SupportedTool
import de.fraunhofer.iem.kpiCalculator.model.kpi.RawValueKpi
import de.fraunhofer.iem.spha.cli.StrictModeConstraintFailed
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream
import java.nio.file.FileSystem

data class TransformerOptions(
    val tool : SupportedTool,
    val inputFiles : List<String>? = null
)

internal interface RawKpiTransformer {
    fun getRawKpis(options: TransformerOptions, strictMode: Boolean) : Collection<RawValueKpi>
}

internal class Tool2RawKpiTransformer : RawKpiTransformer, KoinComponent{

    private val _fileSystem by inject<FileSystem>()
    private val _logger = KotlinLogging.logger{}

    override fun getRawKpis(options: TransformerOptions, strictMode: Boolean): Collection<RawValueKpi> {

        val result : Collection<AdapterResult> = when (options.tool) {
            SupportedTool.Occmd -> {
                TODO()
//                val adapterInput : OccmdDto = OccmdAdapter.createInputFrom(input)
//                OccmdAdapter.transformDataToKpi(adapterInput)
            }
//            SupportedTool.Trivy ->{
//                getSingleInputStreamFromInputFile(options.inputFiles, strictMode).use {
//                    val adapterInput : TrivyDto = TrivyAdapter.dtoFromJson(it)
//                    TrivyAdapter.transformDataToKpi(adapterInput)
//                }
//            }
            else -> TODO("Tool ${options.tool} is not yet supported.")
        }

        val rawKpis = result.filterIsInstance<RawValueKpi>()

        // If we have unequal counts, we know that adapter returned faulted elements. Thus, we throw in strict mode.
        if (strictMode && rawKpis.count() != result.count()){
            throw StrictModeConstraintFailed("The adapter produced faulted results.")
        }

        return rawKpis
    }

    internal fun getSingleInputStreamFromInputFile(inputFiles : List<String>?, strictMode : Boolean) : InputStream {
        if (inputFiles.isNullOrEmpty()){
            throw IllegalStateException("No input files specified.")
        }

        if (inputFiles.count() > 1) {
            if (strictMode) {
                throw StrictModeConstraintFailed("Expected only one input file.")
            }
            _logger.warn { "Expected only one input file. But go #${inputFiles.count()}. Will use first entry." }
        }

        return _fileSystem.provider().newInputStream(_fileSystem.getPath(inputFiles.first()))
    }
}


