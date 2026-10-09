package com.example.dogapp.feature.identify.di

import com.example.dogapp.feature.identify.data.ImageNetBreedClassifier
import com.example.dogapp.feature.identify.domain.BreedClassifier
import com.example.dogapp.feature.identify.domain.IdentifyBreedUseCase
import com.example.dogapp.feature.identify.presentation.IdentifyViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val identifyModule =
    module {
        single { ImageNetBreedClassifier(get()) } bind BreedClassifier::class
        factoryOf(::IdentifyBreedUseCase)
        viewModelOf(::IdentifyViewModel)
    }
