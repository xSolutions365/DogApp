package com.example.dogapp.app.di

import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.database.buildDogAppDatabase
import org.koin.dsl.module

val databaseModule =
    module {
        single { buildDogAppDatabase(get()) }
        single { get<DogAppDatabase>().sourcedBreedDao() }
        single { get<DogAppDatabase>().favouriteBreedDao() }
        single { get<DogAppDatabase>().dailyDrawDao() }
    }
