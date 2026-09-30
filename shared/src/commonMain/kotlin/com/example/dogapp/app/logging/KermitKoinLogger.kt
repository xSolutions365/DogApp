package com.example.dogapp.app.logging

import co.touchlab.kermit.Logger
import org.koin.core.logger.Level
import org.koin.core.logger.MESSAGE
import org.koin.core.logger.Logger as KoinLogger

const val KOIN_LOG_TAG = "Koin"

class KermitKoinLogger(
    private val logger: Logger = Logger.withTag(KOIN_LOG_TAG),
    level: Level = Level.INFO,
) : KoinLogger(level) {

    override fun display(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.d { msg }
            Level.INFO -> logger.i { msg }
            Level.WARNING -> logger.w { msg }
            Level.ERROR -> logger.e { msg }
            Level.NONE -> Unit
        }
    }
}
