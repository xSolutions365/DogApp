package com.example.dogapp.app.logging

import co.touchlab.kermit.Logger
import coil3.util.Logger as CoilLogger

const val IMAGE_LOG_TAG = "Coil"

class KermitCoilLogger(
    private val logger: Logger = Logger.withTag(IMAGE_LOG_TAG),
    override var minLevel: CoilLogger.Level = CoilLogger.Level.Info,
) : CoilLogger {
    override fun log(
        tag: String,
        level: CoilLogger.Level,
        message: String?,
        throwable: Throwable?,
    ) {
        val text = "$tag: ${message.orEmpty()}"
        when (level) {
            CoilLogger.Level.Verbose -> logger.v(throwable) { text }
            CoilLogger.Level.Debug -> logger.d(throwable) { text }
            CoilLogger.Level.Info -> logger.i(throwable) { text }
            CoilLogger.Level.Warn -> logger.w(throwable) { text }
            CoilLogger.Level.Error -> logger.e(throwable) { text }
        }
    }
}
