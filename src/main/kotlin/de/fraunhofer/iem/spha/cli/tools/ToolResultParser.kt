/*
 * Copyright (c) 2025 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.tools

import de.fraunhofer.iem.spha.model.adapter.ToolResult
import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object ToolResultParser {

    object ToolResultParser {
        fun parseJsonFilesFromDirectory(
            directoryPath: String,
            serializers: List<KSerializer<out ToolResult>>,
        ): Map<File, Any> {

            val jsonParser = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }

            val directory = File(directoryPath)
            val parsedObjects = mutableMapOf<File, Any>()

            if (!directory.exists() || !directory.isDirectory) {
                println("Error: Directory not found at path: $directoryPath")
                return emptyMap()
            }

            val jsonFiles =
                directory.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) }
                    ?: emptyArray()

            if (jsonFiles.isEmpty()) {
                println("No .json files found in directory: $directoryPath")
                return emptyMap()
            }

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
                            parsedObjects[file] = resultObject
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

                    if (!parsedObjects.containsKey(file)) {
                        println(
                            "Warning: Could not parse '${file.name}'. No matching data class found."
                        )
                    }
                } catch (e: Exception) {
                    println("Error reading file '${file.name}': ${e.message}")
                }
            }

            return parsedObjects
        }
    }
}
