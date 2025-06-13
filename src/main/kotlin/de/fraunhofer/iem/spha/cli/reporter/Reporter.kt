/*
 * Copyright (c) 2024-2025 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.reporter

import de.fraunhofer.iem.spha.model.kpi.KpiType
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultHierarchy
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultNode

fun getName(kpiType: String): String {
    return when (kpiType) {
        KpiType.CHECKED_IN_BINARIES.name -> "Checked in binaries"
        KpiType.NUMBER_OF_COMMITS.name -> "Number of commits"
        KpiType.CODE_VULNERABILITY_SCORE.name -> "Code vulnerability scores"
        KpiType.CONTAINER_VULNERABILITY_SCORE.name -> "Container vulnerability scores"
        KpiType.NUMBER_OF_SIGNED_COMMITS.name -> "Number of signed commits"
        KpiType.IS_DEFAULT_BRANCH_PROTECTED.name -> "Default branch protection"
        KpiType.SECRETS.name -> "Secrets"
        KpiType.SAST_USAGE.name -> "SAST usage"
        KpiType.COMMENTS_IN_CODE.name -> "Code comments"
        KpiType.DOCUMENTATION_INFRASTRUCTURE.name -> "Documentation infrastructure"
        KpiType.LIB_DAYS_DEV.name -> "LibDays for dev dependencies"
        KpiType.LIB_DAYS_PROD.name -> "LibDays for prod dependencies"
        KpiType.SIGNED_COMMITS_RATIO.name -> "Ratio signed to unsigned commits"
        KpiType.INTERNAL_QUALITY.name -> "⭐ Internal quality"
        KpiType.EXTERNAL_QUALITY.name -> "⭐ External quality"
        KpiType.PROCESS_COMPLIANCE.name -> "✅ Process compliance"
        KpiType.PROCESS_TRANSPARENCY.name -> "\uD83D\uDD0E Process transparency"
        KpiType.SECURITY.name -> "\uD83D\uDD12 Security"
        KpiType.MAXIMAL_VULNERABILITY.name -> "\uD83D\uDEA8 Maximum vulnerability score"
        KpiType.DOCUMENTATION.name -> "\uD83D\uDCD6 Documentation"
        KpiType.ROOT.name -> "\uD83D\uDC96 Software Product Health Score"
        else -> "Unknown"
    }
}

fun KpiResultNode.getScoreVisualization(): String? {
    return when (this.result) {
        is KpiCalculationResult.Empty ->
            "${getName(this.typeId)}: ${(this.result as KpiCalculationResult.Empty).reason}"
        is KpiCalculationResult.Error ->
            "${getName(this.typeId)}: ${(this.result as KpiCalculationResult.Error).reason}"
        is KpiCalculationResult.Incomplete ->
            "${getName(this.typeId)}: ${(this.result as KpiCalculationResult.Incomplete).score} / 100"
        is KpiCalculationResult.Success ->
            "${getName(this.typeId)}: ${(this.result as KpiCalculationResult.Success).score} / 100"
    }
}

fun KpiResultHierarchy.getMarkdown(): String? {

    val topLevelScore = this.rootNode.getScoreVisualization()

    val firstLevelScores =
        this.rootNode.children
            .map { edge -> edge.target.getScoreVisualization() }
            .reduce { a, b ->
                """$a   
                    | $b"""
                    .trimMargin()
            }

    return """# $topLevelScore 
        | ## Top level KPI Scores
        | $firstLevelScores
            """
        .trimMargin()
}
