package com.example.dogapp.app.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

const val APP_LOG_TAG = "DogApp"

fun configureLogging(
    debug: Boolean,
    logWriter: LogWriter = platformLogWriter(),
) {
    Logger.setTag(APP_LOG_TAG)
    if (debug) {
        Logger.setMinSeverity(Severity.Debug)
        Logger.setLogWriters(logWriter)
    } else {
        Logger.setMinSeverity(Severity.Assert)
        Logger.setLogWriters(emptyList())
    }
}
