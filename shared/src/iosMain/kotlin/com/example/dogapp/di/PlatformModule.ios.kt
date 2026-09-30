package com.example.dogapp.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.dogapp.core.datastore.createPreferencesDataStore
import com.example.dogapp.core.datastore.preferencesDataStorePath
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> { createPreferencesDataStore(::preferencesDataStorePath) }
}
