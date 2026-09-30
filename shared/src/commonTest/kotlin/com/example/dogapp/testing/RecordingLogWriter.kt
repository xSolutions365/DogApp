package com.example.dogapp.testing

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

class RecordingLogWriter : LogWriter() {

    data class Entry(val severity: Severity, val message: String, val tag: String)

    val entries = mutableListOf<Entry>()

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        entries += Entry(severity, message, tag)
    }
}
