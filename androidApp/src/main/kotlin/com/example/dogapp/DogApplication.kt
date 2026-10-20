package com.example.dogapp

import android.app.Application
import com.example.dogapp.app.initApp
import com.example.dogapp.core.data.security.SIGNING_CERT_SHA256_PROPERTY
import org.koin.android.ext.koin.androidContext

class DogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initApp(debug = BuildConfig.DEBUG) {
            androidContext(this@DogApplication)
            properties(mapOf(SIGNING_CERT_SHA256_PROPERTY to BuildConfig.SIGNING_CERT_SHA256))
        }
    }
}
