/*
 * Copyright (c) 2025 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.fraunhofer.iem.spha.cli.SphaToolCommandBase
import de.fraunhofer.iem.spha.cli.network.GitHubProjectFetcher
import de.fraunhofer.iem.spha.core.KpiCalculator
import de.fraunhofer.iem.spha.model.kpi.RawValueKpi
import de.fraunhofer.iem.spha.model.kpi.hierarchy.DefaultHierarchy
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiHierarchy
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultHierarchy
import java.nio.file.FileSystem
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class AnalyzeRepositoryCommand :
    SphaToolCommandBase(
        name = "analyze",
        help =
            "Gathers project information from GitHub, transforms tool results into KPIs, and calculates the KPI Hierarchy.",
    ),
    KoinComponent {

    private val fileSystem by inject<FileSystem>()
    private val githubProjectFetcher = GitHubProjectFetcher()

    private val toolResultDir by
        option(
            "-t",
            "--toolResultDir",
            help =
                "The directory to read in JSON tool result files. Default is the current working directory.",
        )

    private val repoUrl by
        option(
                "-r",
                "--repoUrl",
                help =
                    "The project's repository URL. This is used to gather project information, such as the project's name and used technologies.",
            )
            .required()

    private val hierarchy by
        option(
            "-h",
            "--hierarchy",
            help =
                "Optional kpi hierarchy definition file. When not specified the default kpi hierarchy is used.",
        )

    private val output by option("-o", "--output", help = "The result file path.").required()

    override suspend fun run() {
        super.run()

        val githubToken = System.getenv("GITHUB_TOKEN")
        if (githubToken == null) {
            Logger.error { "GITHUB_TOKEN environment variable not set." }
            return
        }

        // Use runBlocking to call the suspend function from a non-suspend context
        val projectInfo = githubProjectFetcher.use { it.getProjectInfo(repoUrl, githubToken) }
        Logger.info { "Fetched project info: $projectInfo" }

        val rawValueKpis = emptyList<RawValueKpi>() // TODO
        if (rawValueKpis.isEmpty()) {
            Logger.warn { "No kpi values to calculate." }
        }

        val hierarchyModel = getHierarchy()
        val kpiResult = KpiCalculator.calculateKpis(hierarchyModel, rawValueKpis)
        writeHierarchy(kpiResult)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeHierarchy(kpiResult: KpiResultHierarchy) {
        val outputFilePath = fileSystem.getPath(output)

        val directory = outputFilePath.toAbsolutePath().parent
        directory.createDirectories()

        outputFilePath.outputStream().use { Json.encodeToStream(kpiResult, it) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getHierarchy(): KpiHierarchy {
        if (hierarchy == null) return DefaultHierarchy.get()

        fileSystem.getPath(hierarchy!!).inputStream().use {
            return Json.decodeFromStream<KpiHierarchy>(it)
        }
    }
}
