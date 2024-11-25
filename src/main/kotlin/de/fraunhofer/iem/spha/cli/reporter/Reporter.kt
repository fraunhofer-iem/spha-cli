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

fun KpiId.getName(): String {
    return when (this) {
        KpiId.CHECKED_IN_BINARIES -> "Checked in binaries"
        KpiId.NUMBER_OF_COMMITS -> "Number of commits"
        KpiId.CODE_VULNERABILITY_SCORE -> "Code vulnerability scores"
        KpiId.CONTAINER_VULNERABILITY_SCORE -> "Container vulnerability scores"
        KpiId.NUMBER_OF_SIGNED_COMMITS -> "Number of signed commits"
        KpiId.IS_DEFAULT_BRANCH_PROTECTED -> "Default branch protection"
        KpiId.SECRETS -> "Secrets"
        KpiId.SAST_USAGE -> "SAST usage"
        KpiId.COMMENTS_IN_CODE -> "Code comments"
        KpiId.DOCUMENTATION_INFRASTRUCTURE -> "Documentation infrastructure"
        KpiId.LIB_DAYS_DEV -> "LibDays for dev dependencies"
        KpiId.LIB_DAYS_PROD -> "LibDays for prod dependencies"
        KpiId.SIGNED_COMMITS_RATIO -> "Ratio signed to unsigned commits"
        KpiId.INTERNAL_QUALITY -> "⭐ Internal quality"
        KpiId.EXTERNAL_QUALITY -> "⭐ External quality"
        KpiId.PROCESS_COMPLIANCE -> "✅ Process compliance"
        KpiId.PROCESS_TRANSPARENCY -> "\uD83D\uDD0E Process transparency"
        KpiId.SECURITY -> "\uD83D\uDD12 Security"
        KpiId.MAXIMAL_VULNERABILITY -> "\uD83D\uDEA8 Maximum vulnerability score"
        KpiId.DOCUMENTATION -> "\uD83D\uDCD6 Documentation"
        KpiId.ROOT -> "\uD83D\uDC96 Software Product Health Score"
    }
}

fun KpiResultNode.getScoreVisualization(): String? {
    return when (this.kpiResult) {
        is KpiCalculationResult.Empty ->
            "${this.kpiId.getName()}: ${(this.kpiResult as KpiCalculationResult.Empty).reason}"
        is KpiCalculationResult.Error ->
            "${this.kpiId.getName()}: ${(this.kpiResult as KpiCalculationResult.Error).reason}"
        is KpiCalculationResult.Incomplete ->
            "${this.kpiId.getName()}: ${(this.kpiResult as KpiCalculationResult.Incomplete).score} / 100"
        is KpiCalculationResult.Success ->
            "${this.kpiId.getName()}: ${(this.kpiResult as KpiCalculationResult.Success).score} / 100"
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
