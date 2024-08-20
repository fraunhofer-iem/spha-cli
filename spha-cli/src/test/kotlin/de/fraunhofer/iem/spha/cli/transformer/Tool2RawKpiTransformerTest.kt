package de.fraunhofer.iem.spha.cli.transformer

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import de.fraunhofer.iem.spha.cli.StrictModeConstraintFailed
import de.fraunhofer.iem.spha.cli.appModules
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declare
import org.mockito.Mockito
import java.nio.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class Tool2RawKpiTransformerTest : KoinTest {
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

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun getSingleInputStreamFromInputFile_NullInput_Throws(strict: Boolean) {
        val command = Tool2RawKpiTransformer()
        assertThrows<IllegalStateException> { command.getSingleInputStreamFromInputFile(null, strict) }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun getSingleInputStreamFromInputFile_Empty_Throws(strict: Boolean) {
        val command = Tool2RawKpiTransformer()
        assertThrows<IllegalStateException> { command.getSingleInputStreamFromInputFile(listOf(), strict) }
    }

    @Test
    fun getSingleInputStreamFromInputFile_MultipleInputs_Strict_Throws() {
        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }
        fileSystem.provider().newOutputStream(fileSystem.getPath("a")).use {
            it.write(123)
        }
        fileSystem.provider().newOutputStream(fileSystem.getPath("b")).use {
            it.write(789)
        }

        val command = Tool2RawKpiTransformer()
        assertThrows<StrictModeConstraintFailed> { command.getSingleInputStreamFromInputFile(listOf("a", "b"), true) }
    }

    @Test
    fun getSingleInputStreamFromInputFile_MultipleInputs_NonStrict_TakeFirst() {
        val fileSystem = declare<FileSystem> { Jimfs.newFileSystem(Configuration.forCurrentPlatform()) }
        fileSystem.provider().newOutputStream(fileSystem.getPath("a")).use { it.write(123) }
        fileSystem.provider().newOutputStream(fileSystem.getPath("b")).use { it.write(789) }

        val command = Tool2RawKpiTransformer()
        command.getSingleInputStreamFromInputFile(listOf("a", "b"), false).use {
            assertEquals(123, it.read())
        }
    }
}
