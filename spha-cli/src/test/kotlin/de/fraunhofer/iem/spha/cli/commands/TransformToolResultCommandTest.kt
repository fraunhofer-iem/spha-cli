package de.fraunhofer.iem.spha.cli.commands

import com.github.ajalt.clikt.testing.test
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import de.fraunhofer.iem.kpiCalculator.adapter.tools.SupportedTool
import de.fraunhofer.iem.kpiCalculator.adapter.tools.ToolNotFoundException
import de.fraunhofer.iem.kpiCalculator.model.kpi.KpiId
import de.fraunhofer.iem.kpiCalculator.model.kpi.RawValueKpi
import de.fraunhofer.iem.spha.cli.appModules
import de.fraunhofer.iem.spha.cli.transformer.RawKpiTransformer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declare
import org.koin.test.mock.declareMock
import org.mockito.BDDMockito.anyBoolean
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import java.nio.file.FileSystem
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TransformToolResultCommandTest : KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestRule = KoinTestExtension.create {
        printLogger(Level.DEBUG)
        modules(appModules)
    }

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Test
    fun testTransform_ToolDoesNotExists_Throws() {
        val command = TransformToolResultCommand()
        assertThrows<ToolNotFoundException> { command.test("-t toolDoesNotExist") }
    }

    @Test
    fun testTransform_TransformerInternal_Throws() {

        val toolName = SupportedTool.Occmd.name

        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.unix()) }

        declareMock<RawKpiTransformer> {
            given(getRawKpis(any(), eq(true))).willThrow(IllegalStateException())
        }

        val expectedResultPath = fileSystem.getPath("$toolName-result.json").toAbsolutePath()
        expectedResultPath.writeText("someJunk...")

        assertThrows<IllegalStateException> { TransformToolResultCommand().test("-t $toolName") }
        // In case of failure, existing file should not be overwritten.
        assertEquals("someJunk...", expectedResultPath.readText())
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun testTransform_StrictModeApplied(expectedStrict: Boolean) {
        val toolName = SupportedTool.Occmd.name
        declare<FileSystem> { Jimfs.newFileSystem(Configuration.unix()) }

        val transformer = declareMock<RawKpiTransformer>()
        val strictCommandInput = if (expectedStrict) "--strict" else ""
        TransformToolResultCommand().test("$strictCommandInput -t $toolName")

        Mockito.verify(transformer,times(1)).getRawKpis(any(), eq(expectedStrict))
    }

    @Test
    fun testTransform_ResultFileOverwrite() {

        val toolName = SupportedTool.Occmd.name

        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.unix()) }

        val transformer = declareMock<RawKpiTransformer> {
            given(getRawKpis(any(), anyBoolean()))
                .willReturn(listOf())
        }

        val expectedResultPath = fileSystem.getPath("$toolName-result.json").toAbsolutePath()
        expectedResultPath.writeText("someJunk...")

        TransformToolResultCommand().test("-t $toolName")

        assertTrue { fileSystem.provider().exists(expectedResultPath) }
        assertEquals("[]", expectedResultPath.readText())

        Mockito.verify(transformer,times(1)).getRawKpis(any(), anyBoolean())
    }

    @Test
    fun testTransform_ResultFileSerialize() {

        val toolName = SupportedTool.Occmd.name

        val resultList = listOf(
            RawValueKpi(KpiId.SECRETS, 100),
            RawValueKpi(KpiId.SECURITY, 1))

        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.unix()) }

        declareMock<RawKpiTransformer> {
            given(getRawKpis(any(), anyBoolean()))
                .willReturn(resultList)
        }

        TransformToolResultCommand().test("-t $toolName")

        val expectedResultPath = fileSystem.getPath("$toolName-result.json").toAbsolutePath()

        // Read in the written file and check if it matches the result
        val actualRawKpis = Json.decodeFromString<Collection<RawValueKpi>>(expectedResultPath.readText())
        assertEquals(actualRawKpis, resultList)
    }

    @ParameterizedTest
    @MethodSource("outputTestSource")
    fun testTransform_UseOutputPath(toolName: String, output: String, expectedFilePath: String) {

        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.unix()) }
        declareMock<RawKpiTransformer> { given(getRawKpis(any(), anyBoolean())).willReturn(listOf()) }

        TransformToolResultCommand().test("-t $toolName -o $output")

        assertTrue { fileSystem.provider().exists(fileSystem.getPath(expectedFilePath)) }
    }

    companion object{
        @JvmStatic
        fun outputTestSource(): List<Arguments> {
            val toolName = "Occmd"
            return listOf(
                arguments("Occmd", ".", "/work/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}"),
                arguments("Occmd", "dir", "/work/dir/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}"),
                arguments("Occmd", "/other/dir", "/other/dir/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}"),
                // This is a misuse, but it should work nether the less.
                arguments("Occmd", "/file.txt", "/file.txt/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}")
            )
        }
    }
}
