package com.example.dogapp.app.di

import com.example.dogapp.feature.breeddetail.di.breedDetailModule
import com.example.dogapp.feature.explore.di.exploreModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

val appModules: List<Module> = listOf(platformModule, networkModule, databaseModule, breedModule, exploreModule, breedDetailModule)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModules)
    }
