package com.example.dogapp.app

import coil3.SingletonImageLoader
import com.example.dogapp.app.di.initKoin
import com.example.dogapp.app.image.createImageLoader
import com.example.dogapp.app.logging.KermitKoinLogger
import com.example.dogapp.app.logging.configureLogging
import org.koin.dsl.KoinAppDeclaration

fun initApp(
    debug: Boolean,
    appDeclaration: KoinAppDeclaration = {},
) {
    configureLogging(debug)
    val koinApplication =
        initKoin {
            logger(KermitKoinLogger())
            appDeclaration()
        }
    SingletonImageLoader.setSafe { context -> createImageLoader(context, koinApplication.koin.get()) }
}
