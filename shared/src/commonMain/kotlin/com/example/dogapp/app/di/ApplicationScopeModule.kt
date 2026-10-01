package com.example.dogapp.app.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val applicationScopeModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    }
