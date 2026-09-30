package com.example.dogapp.app.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.example.dogapp.testing.RecordingLogWriter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoggingTest {

    private val testLogWriter = RecordingLogWriter()

    @AfterTest
    fun tearDown() {
        configureLogging(debug = false)
    }

    @Test
    fun debugBuildsLogDebugAndAboveWithAppTag() {
        configureLogging(debug = true, logWriter = testLogWriter)

        Logger.v { "verbose" }
        Logger.d { "debug" }
        Logger.e { "error" }

        assertEquals(
            listOf(Severity.Debug to "debug", Severity.Error to "error"),
            testLogWriter.entries.map { it.severity to it.message },
        )
        assertTrue(testLogWriter.entries.all { it.tag == APP_LOG_TAG })
    }

    @Test
    fun releaseBuildsLogNothing() {
        configureLogging(debug = false, logWriter = testLogWriter)

        Logger.e { "error" }
        Logger.a { "assert" }

        assertTrue(testLogWriter.entries.isEmpty())
    }
}
