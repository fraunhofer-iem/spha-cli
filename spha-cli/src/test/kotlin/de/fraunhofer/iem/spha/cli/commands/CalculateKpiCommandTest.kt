package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.testing.test
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import de.fraunhofer.iem.kpiCalculator.core.KpiCalculator
import de.fraunhofer.iem.kpiCalculator.model.kpi.KpiId
import de.fraunhofer.iem.kpiCalculator.model.kpi.KpiStrategyId
import de.fraunhofer.iem.kpiCalculator.model.kpi.RawValueKpi
import de.fraunhofer.iem.kpiCalculator.model.kpi.hierarchy.*
import de.fraunhofer.iem.spha.cli.appModules
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkObject
import java.nio.file.FileSystem
import kotlin.io.path.outputStream
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declare

class CalculateKpiCommandTest : KoinTest {
    @JvmField
    @RegisterExtension
    val koinTestRule =
        KoinTestExtension.create {
            printLogger(Level.DEBUG)
            modules(appModules)
        }

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz -> mockkClass(clazz) }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testCalculate_IgnoreIncompatibleFiles() {
        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        fileSystem.getPath("./distract.json").writeText("{ \"someKey\" : 123 }")
        fileSystem.getPath("./distract.txt").writeText("[{ \"kind\" : \"CHECKED_IN_BINARIES\", \"score\" : 100 }]")

        val expectedResult =
            KpiResultHierarchy.create(
                KpiResultNode(
                    KpiId.ROOT,
                    KpiCalculationResult.Success(100),
                    KpiStrategyId.RAW_VALUE_STRATEGY,
                    listOf(),
                )
            )

        mockkObject(KpiCalculator)
        every { KpiCalculator.calculateKpis(DefaultHierarchy.get(), listOf()) } returns
            expectedResult

        val command = CalculateKpiCommand()
        command.test("-o result/h.json")

        fileSystem.provider().newInputStream(fileSystem.getPath("./result/h.json")).use {
            assertEquals(expectedResult, Json.decodeFromStream<KpiResultHierarchy>(it))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testCalculate_ReadRawValuesFromFiles() {
        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        fileSystem.provider().createDirectory(fileSystem.getPath("tools"))
        fileSystem
            .getPath("./tools/1.json")
            .writeText("[{ \"kind\" : \"CHECKED_IN_BINARIES\", \"score\" : 100 }]")
        fileSystem
            .getPath("./tools/2.json")
            .writeText("[{ \"kind\" : \"SECRETS\", \"score\" : 50 }]")

        val expectedResult =
            KpiResultHierarchy.create(
                KpiResultNode(
                    KpiId.ROOT,
                    KpiCalculationResult.Success(100),
                    KpiStrategyId.RAW_VALUE_STRATEGY,
                    listOf(),
                )
            )

        mockkObject(KpiCalculator)
        every {
            KpiCalculator.calculateKpis(
                DefaultHierarchy.get(),
                listOf(RawValueKpi(KpiId.CHECKED_IN_BINARIES, 100), RawValueKpi(KpiId.SECRETS, 50)),
            )
        } returns expectedResult

        val command = CalculateKpiCommand()
        command.test("-o result.json -s tools")

        fileSystem.provider().newInputStream(fileSystem.getPath("result.json")).use {
            assertEquals(expectedResult, Json.decodeFromStream<KpiResultHierarchy>(it))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testCalculate_CustomHierarchy() {

        val customHierarchy =
            KpiHierarchy.create(KpiNode(KpiId.ROOT, KpiStrategyId.MAXIMUM_STRATEGY, listOf()))

        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        fileSystem.getPath("./h.json").outputStream().use {
            Json.encodeToStream(customHierarchy, it)
        }

        val expectedResult =
            KpiResultHierarchy.create(
                KpiResultNode(
                    KpiId.ROOT,
                    KpiCalculationResult.Empty(),
                    KpiStrategyId.RAW_VALUE_STRATEGY,
                    listOf(),
                )
            )

        mockkObject(KpiCalculator)
        every { KpiCalculator.calculateKpis(customHierarchy, listOf()) } returns expectedResult

        val command = CalculateKpiCommand()
        command.test("-o result.json -h h.json")

        fileSystem.provider().newInputStream(fileSystem.getPath("result.json")).use {
            assertEquals(expectedResult, Json.decodeFromStream<KpiResultHierarchy>(it))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testCalculate_Integration() {
        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        fileSystem.provider().createDirectory(fileSystem.getPath("tools"))
        fileSystem
            .getPath("./tools/1.json")
            .writeText("[{ \"kind\" : \"CHECKED_IN_BINARIES\", \"score\" : 100 }]")
        fileSystem
            .getPath("./tools/2.json")
            .writeText("[{ \"kind\" : \"SECRETS\", \"score\" : 50 }]")

        val command = CalculateKpiCommand()
        command.test("-o result.json -s tools")

        fileSystem.provider().newInputStream(fileSystem.getPath("result.json")).use {
            assertEquals(
                KpiCalculator.calculateKpis(
                    DefaultHierarchy.get(),
                    listOf(
                        RawValueKpi(KpiId.CHECKED_IN_BINARIES, 100),
                        RawValueKpi(KpiId.SECRETS, 50),
                    ),
                ),
                Json.decodeFromStream<KpiResultHierarchy>(it),
            )
        }
    }
}
