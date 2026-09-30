package com.example.dogapp.di

import com.example.dogapp.AndroidPlatform
import com.example.dogapp.Platform
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Platform> { AndroidPlatform() }
    single<HttpClientEngine> { OkHttp.create() }
}
