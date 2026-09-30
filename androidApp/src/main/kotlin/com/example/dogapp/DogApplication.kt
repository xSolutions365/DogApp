package com.example.dogapp

import android.app.Application
import com.example.dogapp.app.initApp
import org.koin.android.ext.koin.androidContext

class DogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initApp(debug = BuildConfig.DEBUG) {
            androidContext(this@DogApplication)
        }
    }
}
