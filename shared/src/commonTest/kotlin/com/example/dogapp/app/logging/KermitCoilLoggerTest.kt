package com.example.dogapp.app.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.example.dogapp.testing.RecordingLogWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import coil3.util.Logger as CoilLogger

class KermitCoilLoggerTest {
    @Test
    fun coilLevelsMapToKermitSeveritiesUnderCoilTag() {
        val logWriter = RecordingLogWriter()
        val coilLogger = KermitCoilLogger(logger = Logger(config = loggerConfigInit(logWriter), tag = IMAGE_LOG_TAG))

        CoilLogger.Level.entries.forEach { level -> coilLogger.log("RealImageLoader", level, level.name, null) }

        assertEquals(
            listOf(
                Severity.Verbose to "RealImageLoader: Verbose",
                Severity.Debug to "RealImageLoader: Debug",
                Severity.Info to "RealImageLoader: Info",
                Severity.Warn to "RealImageLoader: Warn",
                Severity.Error to "RealImageLoader: Error",
            ),
            logWriter.entries.map { it.severity to it.message },
        )
        assertEquals(setOf(IMAGE_LOG_TAG), logWriter.entries.map { it.tag }.toSet())
    }
}
