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
import de.fraunhofer.iem.spha.cli.transformer.RawKpiTransformer
import de.fraunhofer.iem.spha.cli.transformer.ToolNotFoundException
import de.fraunhofer.iem.spha.cli.transformer.TransformerOptions
import de.fraunhofer.iem.spha.model.kpi.KpiId
import de.fraunhofer.iem.spha.model.kpi.RawValueKpi
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import java.nio.file.FileSystem
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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

class TransformToolResultCommandTest : KoinTest {

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

    @Test
    fun testTransform_ToolDoesNotExists_Throws() {
        val command = TransformToolResultCommand()
        assertThrows<ToolNotFoundException> { command.test("-t toolDoesNotExist") }
    }

    @Test
    fun testTransform_TransformerInternal_Throws() {

        val toolName = "occmd"

        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        declareMock<RawKpiTransformer> {
            every { getRawKpis(any(), any()) } throws IllegalStateException()
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
        val toolName = "occmd"
        declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        val transformer =
            declareMock<RawKpiTransformer> { every { getRawKpis(any(), any()) } returns listOf() }
        val strictCommandInput = if (expectedStrict) "--strict" else ""
        TransformToolResultCommand().test("$strictCommandInput -t $toolName")

        verify(exactly = 1) { transformer.getRawKpis(any(), eq(expectedStrict)) }
    }

    @Test
    fun testTransform_ResultFileOverwrite() {

        val toolName = "occmd"

        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        val transformer =
            declareMock<RawKpiTransformer> { every { getRawKpis(any(), any()) } returns listOf() }

        val expectedResultPath = fileSystem.getPath("$toolName-result.json").toAbsolutePath()
        expectedResultPath.writeText("someJunk...")

        TransformToolResultCommand().test("-t $toolName")

        assertTrue { fileSystem.provider().exists(expectedResultPath) }
        assertEquals("[]", expectedResultPath.readText())

        verify(exactly = 1) { transformer.getRawKpis(any(), any()) }
    }

    @Test
    fun testTransform_ResultFileSerialize() {

        val toolName = "occmd"

        val resultList = listOf(RawValueKpi(KpiId.SECRETS, 100), RawValueKpi(KpiId.SECURITY, 1))

        val fileSystem =
            declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }

        declareMock<RawKpiTransformer> { every { getRawKpis(any(), any()) } returns resultList }

        TransformToolResultCommand().test("-t $toolName")

        val expectedResultPath = fileSystem.getPath("$toolName-result.json").toAbsolutePath()

        // Read in the written file and check if it matches the result
        val actualRawKpis =
            Json.decodeFromString<Collection<RawValueKpi>>(expectedResultPath.readText())
        assertEquals(actualRawKpis, resultList)
    }

    @ParameterizedTest
    @MethodSource("outputTestSource")
    fun testTransform_UseOutputPath(toolName: String, output: String, expectedFilePath: String) {

        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.unix()) }
        declareMock<RawKpiTransformer> { every { getRawKpis(any(), any()) } returns listOf() }

        TransformToolResultCommand().test("-t $toolName -o $output")

        assertTrue { fileSystem.provider().exists(fileSystem.getPath(expectedFilePath)) }
    }

    @ParameterizedTest
    @MethodSource("inputTestSource")
    fun testTransform_MultipleInputsSplit(inputArg: String, expectedInputs: List<String>) {
        val toolName = "toolA"

        declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }
        val transformer =
            declareMock<RawKpiTransformer> { every { getRawKpis(any(), any()) } returns listOf() }
        TransformToolResultCommand().test("-t $toolName $inputArg")

        val options = TransformerOptions(toolName, inputFiles = expectedInputs)
        verify(exactly = 1) { transformer.getRawKpis(eq(options), any()) }
    }

    companion object {
        @JvmStatic
        fun outputTestSource(): List<Arguments> {
            val toolName = "toolA"
            return listOf(
                arguments(
                    "toolA",
                    ".",
                    "/work/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}",
                ),
                arguments(
                    "toolA",
                    "dir",
                    "/work/dir/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}",
                ),
                arguments(
                    "toolA",
                    "/other/dir",
                    "/other/dir/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}",
                ),
                // This is a misuse, but it should work nether the less.
                arguments(
                    "toolA",
                    "/file.txt",
                    "/file.txt/$toolName${TransformToolResultCommand.RESULT_FILE_SUFFIX}",
                ),
            )
        }

        @JvmStatic
        fun inputTestSource(): List<Arguments> {
            return listOf(
                arguments("-i input.json", listOf("input.json")),
                arguments("--inputFile \"a b.json\"", listOf("a b.json")),
                arguments(
                    "-i a.json --inputFile b.json -i \"d e.json\"",
                    listOf("a.json", "b.json", "d e.json"),
                ),
            )
        }
    }
}
