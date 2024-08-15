package de.fraunhofer.iem.spha.cli.transformer

import de.fraunhofer.iem.kpiCalculator.adapter.tools.SupportedTool
import de.fraunhofer.iem.kpiCalculator.model.kpi.RawValueKpi

data class TransformerOptions(
    val tool : SupportedTool
)

internal interface RawKpiTransformer {
    fun getRawKpis(options: TransformerOptions, strictMode: Boolean) : Collection<RawValueKpi>
}

internal class Tool2RawKpiTransformer : RawKpiTransformer{
    override fun getRawKpis(options: TransformerOptions, strictMode: Boolean): Collection<RawValueKpi> {

//        val result : Collection<AdapterResult> = when (tool){
//            SupportedTool.Occmd -> {
////                val adapterInput = OccmdAdapter.createInputFrom(input)
////                OccmdAdapter.transformDataToKpi(adapterInput)
//                throw NotImplementedError()
//            }
//        }

        throw NotImplementedError()
    }
}
