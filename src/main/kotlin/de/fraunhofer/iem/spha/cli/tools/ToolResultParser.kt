/*
 * Copyright (c) 2025 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.tools

import de.fraunhofer.iem.spha.adapter.AdapterResult
import de.fraunhofer.iem.spha.adapter.tools.osv.OsvAdapter
import de.fraunhofer.iem.spha.adapter.tools.tlc.TlcAdapter
import de.fraunhofer.iem.spha.adapter.tools.trivy.TrivyAdapter
import de.fraunhofer.iem.spha.adapter.tools.trufflehog.TrufflehogAdapter
import de.fraunhofer.iem.spha.model.adapter.*
import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object ToolResultParser {

    fun parseJsonFilesFromDirectory(
        directoryPath: String,
        serializers: List<KSerializer<out ToolResult>>,
    ): List<AdapterResult<*>> {

        val jsonParser = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        val directory = File(directoryPath)

        if (!directory.exists() || !directory.isDirectory) {
            println("Error: Directory not found at path: $directoryPath")
            return emptyList()
        }

        val jsonFiles =
            directory.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) }
                ?: emptyArray()

        if (jsonFiles.isEmpty()) {
            println("No .json files found in directory: $directoryPath")
            return emptyList()
        }

        val adapterResults = mutableListOf<AdapterResult<*>>()

        for (file in jsonFiles) {
            try {
                val content = file.readText(Charsets.UTF_8)
                if (content.isBlank()) {
                    println("Warning: Skipping empty file: ${file.name}")
                    continue
                }

                for (serializer in serializers) {

                    try {
                        val resultObject = jsonParser.decodeFromString(serializer, content)
                        adapterResults.addAll(
                            when (resultObject) {
                                is OsvScannerDto -> {
                                    OsvAdapter.transformDataToKpi(resultObject)
                                }
                                is TrivyDtoV2 -> {
                                    TrivyAdapter.transformTrivyV2ToKpi(listOf(resultObject))
                                }
                                is TrufflehogReportDto -> {
                                    TrufflehogAdapter.transformDataToKpi(resultObject)
                                }
                                is TlcDto -> {
                                    TlcAdapter.transformDataToKpi(listOf(resultObject))
                                }
                                else -> {
                                    println("Unknown result object")
                                    emptyList()
                                }
                            }
                        )
                        println(
                            "Successfully parsed '${file.name}' as '${serializer.descriptor.serialName}'"
                        )
                        break
                    } catch (e: SerializationException) {
                        // expected, continue
                    } catch (e: Exception) {
                        println("Unexpected error parsing '${file.name}': ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("Error reading file '${file.name}': ${e.message}")
            }
        }

        return adapterResults
    }
}
