package com.example.dogapp.feature.breeddetail.di

import com.example.dogapp.feature.breeddetail.presentation.BreedDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val breedDetailModule =
    module {
        viewModelOf(::BreedDetailViewModel)
    }
