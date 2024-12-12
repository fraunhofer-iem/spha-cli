/*
 * Copyright (c) 2024 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

package de.fraunhofer.iem.spha.cli.reporter

import de.fraunhofer.iem.spha.model.kpi.KpiId
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultHierarchy
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultNode

fun getName(kpiId: String): String {
    return when (kpiId) {
        KpiId.CHECKED_IN_BINARIES.name -> "Checked in binaries"
        KpiId.NUMBER_OF_COMMITS.name -> "Number of commits"
        KpiId.CODE_VULNERABILITY_SCORE.name -> "Code vulnerability scores"
        KpiId.CONTAINER_VULNERABILITY_SCORE.name -> "Container vulnerability scores"
        KpiId.NUMBER_OF_SIGNED_COMMITS.name -> "Number of signed commits"
        KpiId.IS_DEFAULT_BRANCH_PROTECTED.name -> "Default branch protection"
        KpiId.SECRETS.name -> "Secrets"
        KpiId.SAST_USAGE.name -> "SAST usage"
        KpiId.COMMENTS_IN_CODE.name -> "Code comments"
        KpiId.DOCUMENTATION_INFRASTRUCTURE.name -> "Documentation infrastructure"
        KpiId.LIB_DAYS_DEV.name -> "LibDays for dev dependencies"
        KpiId.LIB_DAYS_PROD.name -> "LibDays for prod dependencies"
        KpiId.SIGNED_COMMITS_RATIO.name -> "Ratio signed to unsigned commits"
        KpiId.INTERNAL_QUALITY.name -> "⭐ Internal quality"
        KpiId.EXTERNAL_QUALITY.name -> "⭐ External quality"
        KpiId.PROCESS_COMPLIANCE.name -> "✅ Process compliance"
        KpiId.PROCESS_TRANSPARENCY.name -> "\uD83D\uDD0E Process transparency"
        KpiId.SECURITY.name -> "\uD83D\uDD12 Security"
        KpiId.MAXIMAL_VULNERABILITY.name -> "\uD83D\uDEA8 Maximum vulnerability score"
        KpiId.DOCUMENTATION.name -> "\uD83D\uDCD6 Documentation"
        KpiId.ROOT.name -> "\uD83D\uDC96 Software Product Health Score"
        else -> "Unknown"
    }
}

fun KpiResultNode.getScoreVisualization(): String? {
    return when (this.kpiResult) {
        is KpiCalculationResult.Empty ->
            "${getName(this.kpiId)}: ${(this.kpiResult as KpiCalculationResult.Empty).reason}"
        is KpiCalculationResult.Error ->
            "${getName(this.kpiId)}: ${(this.kpiResult as KpiCalculationResult.Error).reason}"
        is KpiCalculationResult.Incomplete ->
            "${getName(this.kpiId)}: ${(this.kpiResult as KpiCalculationResult.Incomplete).score} / 100"
        is KpiCalculationResult.Success ->
            "${getName(this.kpiId)}: ${(this.kpiResult as KpiCalculationResult.Success).score} / 100"
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
