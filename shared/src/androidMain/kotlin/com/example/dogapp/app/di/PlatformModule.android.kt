package com.example.dogapp.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.dogapp.core.data.datastore.PREFERENCES_FILE_NAME
import com.example.dogapp.core.data.datastore.createPreferencesDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<DataStore<Preferences>> {
        createPreferencesDataStore {
            androidContext().filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath
        }
    }
}
