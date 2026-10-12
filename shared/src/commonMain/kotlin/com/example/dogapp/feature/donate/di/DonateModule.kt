package com.example.dogapp.feature.donate.di

import com.example.dogapp.feature.donate.data.MockCharityRepository
import com.example.dogapp.feature.donate.domain.CharityRepository
import com.example.dogapp.feature.donate.presentation.CharitiesViewModel
import com.example.dogapp.feature.donate.presentation.CharityViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val donateModule =
    module {
        single { MockCharityRepository() } bind CharityRepository::class
        viewModelOf(::CharitiesViewModel)
        viewModelOf(::CharityViewModel)
    }
