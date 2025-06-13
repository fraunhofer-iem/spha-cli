/*
 * Copyright (c) 2024-2025 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.transformer

import de.fraunhofer.iem.spha.adapter.AdapterResult
import de.fraunhofer.iem.spha.adapter.tools.osv.OsvAdapter
import de.fraunhofer.iem.spha.adapter.tools.trivy.TrivyAdapter
import de.fraunhofer.iem.spha.adapter.tools.trufflehog.TrufflehogAdapter
import de.fraunhofer.iem.spha.cli.StrictModeConstraintFailed
import de.fraunhofer.iem.spha.model.adapter.osv.OsvScannerDto
import de.fraunhofer.iem.spha.model.adapter.trivy.TrivyDto
import de.fraunhofer.iem.spha.model.adapter.trufflehog.TrufflehogReportDto
import de.fraunhofer.iem.spha.model.kpi.RawValueKpi
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.InputStream
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

        val result: Collection<AdapterResult<*>> =
            when (options.tool) {
                //            "occmd" -> {
                //                val adapterInput: OccmdDto = OccmdAdapter.createInputFrom(input)
                //                OccmdAdapter.transformDataToKpi(adapterInput)
                //            }
                "trivy" -> {
                    getSingleInputStreamFromInputFile(options.inputFiles, strictMode).use {
                        _logger.info { "Selected supported tool: Trivy" }
                        val adapterInput: TrivyDto = TrivyAdapter.dtoFromJson(it)
                        return@use TrivyAdapter.transformDataToKpi(adapterInput)
                    }
                }
                "osv" -> {
                    getSingleInputStreamFromInputFile(options.inputFiles, strictMode).use {
                        _logger.info { "Selected supported tool: OSV" }
                        val adapterInput: OsvScannerDto = OsvAdapter.dtoFromJson(it)
                        return@use OsvAdapter.transformDataToKpi(adapterInput)
                    }
                }
                "trufflehog" -> {
                    getSingleInputStreamFromInputFile(options.inputFiles, strictMode).use {
                        _logger.info { "Selected supported tool: Trufflehog" }
                        val adapterInput: List<TrufflehogReportDto> =
                            TrufflehogAdapter.dtoFromJson(it)
                        return@use TrufflehogAdapter.transformDataToKpi(adapterInput)
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

    internal fun getSingleInputStreamFromInputFile(
        inputFiles: List<String>?,
        strictMode: Boolean,
    ): InputStream {
        check(!inputFiles.isNullOrEmpty()) { "No input files specified." }

        if (inputFiles.count() > 1) {
            if (strictMode) {
                throw StrictModeConstraintFailed("Expected only one input file.")
            }
            _logger.warn {
                "Expected only one input file. But go #${inputFiles.count()}. Will use first entry."
            }
        }

        return _fileSystem.provider().newInputStream(_fileSystem.getPath(inputFiles.first()))
    }
}
