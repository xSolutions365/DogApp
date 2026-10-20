package com.example.dogapp.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dogapp.core.data.database.DATABASE_FILE_NAME
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.datastore.PREFERENCES_FILE_NAME
import com.example.dogapp.core.data.datastore.createPreferencesDataStore
import com.example.dogapp.core.data.security.AndroidDeviceIntegrityChecker
import com.example.dogapp.core.data.security.PinReportingInterceptor
import com.example.dogapp.core.data.security.SIGNING_CERT_SHA256_PROPERTY
import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import com.example.dogapp.feature.identify.data.ImageLabeler
import com.example.dogapp.feature.identify.data.LiteRtImageLabeler
import com.example.dogapp.feature.wallet.data.BiometricPromptHost
import com.example.dogapp.feature.wallet.data.KeystoreWalletVault
import com.example.dogapp.feature.wallet.data.WalletVault
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { OkHttp.create { addNetworkInterceptor(PinReportingInterceptor(get())) } }
        single<DataStore<Preferences>> {
            createPreferencesDataStore {
                androidContext().filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath
            }
        }
        single<RoomDatabase.Builder<DogAppDatabase>> {
            Room.databaseBuilder<DogAppDatabase>(
                context = androidContext(),
                name = androidContext().getDatabasePath(DATABASE_FILE_NAME).absolutePath,
            )
        }
        single<ImageLabeler> { LiteRtImageLabeler(androidContext()) }
        single<DeviceIntegrityChecker> {
            AndroidDeviceIntegrityChecker(androidContext(), getProperty(SIGNING_CERT_SHA256_PROPERTY, ""))
        }
        single<WalletVault>(createdAtStart = true) {
            KeystoreWalletVault(androidContext(), get(), BiometricPromptHost(androidContext()))
        }
    }
