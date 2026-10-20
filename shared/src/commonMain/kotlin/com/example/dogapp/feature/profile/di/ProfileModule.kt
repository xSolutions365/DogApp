package com.example.dogapp.feature.profile.di

import com.example.dogapp.feature.profile.presentation.DeviceChecksViewModel
import com.example.dogapp.feature.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule =
    module {
        viewModelOf(::ProfileViewModel)
        viewModelOf(::DeviceChecksViewModel)
    }
