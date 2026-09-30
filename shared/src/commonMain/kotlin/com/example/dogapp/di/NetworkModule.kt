package com.example.dogapp.di

import com.example.dogapp.core.network.HttpClientFactory
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create(get()) }
}
