package com.example.dogapp.di

import com.example.dogapp.IOSPlatform
import com.example.dogapp.Platform
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Platform> { IOSPlatform() }
    single<HttpClientEngine> { Darwin.create() }
}
