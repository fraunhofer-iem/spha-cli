package de.fraunhofer.iem.spha.cli.util

import de.fraunhofer.iem.spha.cli.StrictModeConstraintFailed
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.InputStream
import java.nio.file.FileSystem

internal object InputHelper {

    private val _logger = KotlinLogging.logger {}

    fun getSingleInputStreamFromInputFile(
        fileSystem: FileSystem,
        inputFiles: List<String>?,
        strictMode: Boolean,
    ): InputStream {
        check(!inputFiles.isNullOrEmpty()) { "No input files specified." }

        if (inputFiles.count() > 1) {
            if (strictMode) {
                throw StrictModeConstraintFailed("Expected only one input file.")
            }
            _logger.warn {
                "Expected only one input file. But go #${inputFiles.count()}. Will use first entry."
            }
        }

        return fileSystem.provider().newInputStream(fileSystem.getPath(inputFiles.first()))
    }
}
