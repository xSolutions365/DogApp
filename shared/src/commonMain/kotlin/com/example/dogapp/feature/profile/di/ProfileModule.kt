package com.example.dogapp.feature.profile.di

import com.example.dogapp.feature.profile.presentation.DeviceStateViewModel
import com.example.dogapp.feature.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule =
    module {
        viewModel {
            ProfileViewModel(
                observeFavouriteBreeds = get(),
                observeDrawHistory = get(),
                today = get(),
                removeFavourite = get(),
                integrityChecker = get(),
                breedSync = get(),
            )
        }
        viewModelOf(::DeviceStateViewModel)
    }
