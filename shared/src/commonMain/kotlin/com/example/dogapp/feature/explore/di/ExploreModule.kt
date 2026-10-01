package com.example.dogapp.feature.explore.di

import com.example.dogapp.feature.explore.presentation.ExploreViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val exploreModule =
    module {
        viewModelOf(::ExploreViewModel)
    }
