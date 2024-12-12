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
import de.fraunhofer.iem.spha.model.kpi.KpiStrategyId
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiCalculationResult
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultEdge
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultHierarchy
import de.fraunhofer.iem.spha.model.kpi.hierarchy.KpiResultNode
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class ReporterTest {
    private fun randomKpiResultNode(): KpiResultNode {
        return KpiResultNode(
            kpiId = KpiId.SECURITY.name,
            kpiResult = KpiCalculationResult.Success(score = 65),
            strategyType = KpiStrategyId.WEIGHTED_AVERAGE_STRATEGY,
            children = listOf(),
        )
    }

    private fun randomKpiResultEdge(): KpiResultEdge {
        return KpiResultEdge(
            target = randomKpiResultNode(),
            plannedWeight = 0.3,
            actualWeight = 0.3,
        )
    }

    @Test
    fun markdownForValidResultHierarchy() {
        val kpiResultHierarchy =
            KpiResultHierarchy.create(
                KpiResultNode(
                    kpiId = KpiId.ROOT.name,
                    kpiResult = KpiCalculationResult.Success(score = 75),
                    strategyType = KpiStrategyId.WEIGHTED_AVERAGE_STRATEGY,
                    children =
                        listOf(
                            KpiResultEdge(
                                target =
                                    KpiResultNode(
                                        kpiId = KpiId.SECURITY.name,
                                        kpiResult = KpiCalculationResult.Success(score = 65),
                                        strategyType = KpiStrategyId.WEIGHTED_AVERAGE_STRATEGY,
                                        children =
                                            listOf(randomKpiResultEdge(), randomKpiResultEdge()),
                                    ),
                                plannedWeight = 0.4,
                                actualWeight = 0.4,
                            ),
                            KpiResultEdge(
                                target =
                                    KpiResultNode(
                                        kpiId = KpiId.EXTERNAL_QUALITY.name,
                                        kpiResult = KpiCalculationResult.Success(score = 85),
                                        strategyType = KpiStrategyId.WEIGHTED_AVERAGE_STRATEGY,
                                        children =
                                            listOf(randomKpiResultEdge(), randomKpiResultEdge()),
                                    ),
                                plannedWeight = 0.4,
                                actualWeight = 0.4,
                            ),
                        ),
                )
            )

        val markdown = kpiResultHierarchy.getMarkdown()
        assertNotNull(markdown)
        assertNotEquals("", markdown)
    }
}
