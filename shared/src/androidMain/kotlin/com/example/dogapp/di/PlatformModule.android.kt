package com.example.dogapp.di

import com.example.dogapp.AndroidPlatform
import com.example.dogapp.Platform
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Platform> { AndroidPlatform() }
}
