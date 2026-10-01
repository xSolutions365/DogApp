package com.example.dogapp.app.di

import com.example.dogapp.feature.breeddetail.di.breedDetailModule
import com.example.dogapp.feature.explore.di.exploreModule
import com.example.dogapp.feature.home.di.homeModule
import com.example.dogapp.feature.profile.di.profileModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

val appModules: List<Module> =
    listOf(
        applicationScopeModule,
        platformModule,
        networkModule,
        databaseModule,
        breedModule,
        favouriteModule,
        drawModule,
        homeModule,
        exploreModule,
        breedDetailModule,
        profileModule,
    )

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModules)
    }
