package com.example.dogapp.app.di

import com.example.dogapp.core.data.network.HttpClientFactory
import org.koin.dsl.module

val networkModule =
    module {
        single { HttpClientFactory.create(get()) }
    }
