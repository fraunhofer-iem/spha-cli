/*
 * Copyright (c) 2024 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.testing.test
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import de.fraunhofer.iem.spha.cli.appModules
import java.nio.file.FileSystem
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.mock.declare

class ReportCommandTest : KoinTest {
    @JvmField
    @RegisterExtension
    val koinTestRule =
        KoinTestExtension.create {
            printLogger(Level.DEBUG)
            modules(appModules)
        }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testMarkdown_Integration() {
        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        fileSystem.provider().createDirectory(fileSystem.getPath("result"))
        fileSystem
            .getPath("./result/kpis.json")
            .writeText(
                "{\"rootNode\":{\"kpiId\":\"ROOT\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Incomplete\",\"score\":0,\"reason\":\"Incomplete results.\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"PROCESS_TRANSPARENCY\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"SIGNED_COMMITS_RATIO\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_RATIO_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"NUMBER_OF_COMMITS\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":1.0,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"NUMBER_OF_SIGNED_COMMITS\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":1.0,\"actualWeight\":0.0}]},\"plannedWeight\":1.0,\"actualWeight\":0.0}]},\"plannedWeight\":0.1,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"PROCESS_COMPLIANCE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"CHECKED_IN_BINARIES\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"SIGNED_COMMITS_RATIO\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_RATIO_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"NUMBER_OF_COMMITS\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":1.0,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"NUMBER_OF_SIGNED_COMMITS\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":1.0,\"actualWeight\":0.0}]},\"plannedWeight\":0.2,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"IS_DEFAULT_BRANCH_PROTECTED\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.3,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"DOCUMENTATION\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"DOCUMENTATION_INFRASTRUCTURE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.6,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"COMMENTS_IN_CODE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.4,\"actualWeight\":0.0}]},\"plannedWeight\":0.3,\"actualWeight\":0.0}]},\"plannedWeight\":0.1,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"SECURITY\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Incomplete\",\"score\":0,\"reason\":\"Incomplete results.\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"SECRETS\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"MAXIMAL_VULNERABILITY\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Success\",\"score\":0},\"strategyType\":\"MINIMUM_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"CODE_VULNERABILITY_SCORE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Success\",\"score\":10},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.2},{\"target\":{\"kpiId\":\"CODE_VULNERABILITY_SCORE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Success\",\"score\":34},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.2},{\"target\":{\"kpiId\":\"CODE_VULNERABILITY_SCORE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Success\",\"score\":0},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.2},{\"target\":{\"kpiId\":\"CODE_VULNERABILITY_SCORE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Success\",\"score\":14},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.2},{\"target\":{\"kpiId\":\"CODE_VULNERABILITY_SCORE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Success\",\"score\":47},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.2,\"actualWeight\":0.2}]},\"plannedWeight\":0.35,\"actualWeight\":1.0},{\"target\":{\"kpiId\":\"MAXIMAL_VULNERABILITY\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"MAXIMUM_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"CONTAINER_VULNERABILITY_SCORE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":1.0,\"actualWeight\":0.0}]},\"plannedWeight\":0.35,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"CHECKED_IN_BINARIES\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.1,\"actualWeight\":0.0}]},\"plannedWeight\":0.4,\"actualWeight\":1.0},{\"target\":{\"kpiId\":\"INTERNAL_QUALITY\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"DOCUMENTATION\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"DOCUMENTATION_INFRASTRUCTURE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.6,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"COMMENTS_IN_CODE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.4,\"actualWeight\":0.0}]},\"plannedWeight\":1.0,\"actualWeight\":0.0}]},\"plannedWeight\":0.15,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"EXTERNAL_QUALITY\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"DOCUMENTATION\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"WEIGHTED_AVERAGE_STRATEGY\",\"children\":[{\"target\":{\"kpiId\":\"DOCUMENTATION_INFRASTRUCTURE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.6,\"actualWeight\":0.0},{\"target\":{\"kpiId\":\"COMMENTS_IN_CODE\",\"kpiResult\":{\"type\":\"de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult.Empty\"},\"strategyType\":\"RAW_VALUE_STRATEGY\",\"children\":[]},\"plannedWeight\":0.4,\"actualWeight\":0.0}]},\"plannedWeight\":1.0,\"actualWeight\":0.0}]},\"plannedWeight\":0.25,\"actualWeight\":0.0}]},\"schemaVersion\":\"1.0.0\"}"
            )

        val command = ReportCommand()
        command.test("-r ./result/kpis.json -o ./result/output.md --markdown")

        fileSystem.provider().newInputStream(fileSystem.getPath("./result/output.md")).use {
            val fileContent = it.bufferedReader().readText()
            println(fileContent)
            assertNotEquals("", fileContent)
        }
    }
}
