package com.example.dogapp.app

import com.example.dogapp.app.di.initKoin
import com.example.dogapp.app.logging.KermitKoinLogger
import com.example.dogapp.app.logging.configureLogging
import org.koin.dsl.KoinAppDeclaration

fun initApp(debug: Boolean, appDeclaration: KoinAppDeclaration = {}) {
    configureLogging(debug)
    initKoin {
        logger(KermitKoinLogger())
        appDeclaration()
    }
}
