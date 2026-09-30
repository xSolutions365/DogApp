package com.example.dogapp.app.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.example.dogapp.testing.RecordingLogWriter
import org.koin.core.logger.Level
import kotlin.test.Test
import kotlin.test.assertEquals

class KermitKoinLoggerTest {
    @Test
    fun koinLevelsMapToKermitSeveritiesUnderKoinTag() {
        val testLogWriter = RecordingLogWriter()
        val koinLogger =
            KermitKoinLogger(
                logger = Logger(config = loggerConfigInit(testLogWriter), tag = KOIN_LOG_TAG),
                level = Level.DEBUG,
            )

        koinLogger.display(Level.DEBUG, "debug")
        koinLogger.display(Level.INFO, "info")
        koinLogger.display(Level.WARNING, "warning")
        koinLogger.display(Level.ERROR, "error")
        koinLogger.display(Level.NONE, "none")

        assertEquals(
            listOf(
                Triple(Severity.Debug, "debug", KOIN_LOG_TAG),
                Triple(Severity.Info, "info", KOIN_LOG_TAG),
                Triple(Severity.Warn, "warning", KOIN_LOG_TAG),
                Triple(Severity.Error, "error", KOIN_LOG_TAG),
            ),
            testLogWriter.entries.map { Triple(it.severity, it.message, it.tag) },
        )
    }
}
