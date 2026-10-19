package com.example.dogapp.app.di

import com.example.dogapp.core.data.config.BuildSecrets
import com.example.dogapp.feature.breeddetail.di.breedDetailModule
import com.example.dogapp.feature.donate.di.donateModule
import com.example.dogapp.feature.explore.di.exploreModule
import com.example.dogapp.feature.home.di.homeModule
import com.example.dogapp.feature.identify.di.identifyModule
import com.example.dogapp.feature.profile.di.profileModule
import com.example.dogapp.feature.wallet.di.walletModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

val appModules: List<Module> =
    listOf(
        applicationScopeModule,
        platformModule,
        networkModule,
        databaseModule,
        breedModule(theDogApiKey = BuildSecrets.THE_DOG_API_KEY, apiNinjasKey = BuildSecrets.API_NINJAS_KEY),
        favouriteModule,
        drawModule,
        homeModule,
        exploreModule,
        breedDetailModule,
        profileModule,
        identifyModule,
        donateModule,
        walletModule,
    )

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModules)
    }
