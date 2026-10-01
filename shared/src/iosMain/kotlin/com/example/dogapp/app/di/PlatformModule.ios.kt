package com.example.dogapp.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dogapp.core.data.database.DATABASE_FILE_NAME
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.datastore.createPreferencesDataStore
import com.example.dogapp.core.data.datastore.preferencesDataStorePath
import com.example.dogapp.core.data.storage.documentsDirectoryPath
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { Darwin.create() }
        single<DataStore<Preferences>> { createPreferencesDataStore(::preferencesDataStorePath) }
        single<RoomDatabase.Builder<DogAppDatabase>> {
            Room.databaseBuilder<DogAppDatabase>(name = documentsDirectoryPath() + "/" + DATABASE_FILE_NAME)
        }
    }
