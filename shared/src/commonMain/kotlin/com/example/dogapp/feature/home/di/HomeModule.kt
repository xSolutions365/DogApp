package com.example.dogapp.feature.home.di

import com.example.dogapp.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule =
    module {
        viewModelOf(::HomeViewModel)
    }
