package com.example.dogapp.feature.donate.di

import com.example.dogapp.feature.donate.data.DefaultDonationRepository
import com.example.dogapp.feature.donate.data.DemoPaymentProcessor
import com.example.dogapp.feature.donate.data.MockCharityRepository
import com.example.dogapp.feature.donate.domain.CharityRepository
import com.example.dogapp.feature.donate.domain.DonateUseCase
import com.example.dogapp.feature.donate.domain.DonationRepository
import com.example.dogapp.feature.donate.domain.PaymentProcessor
import com.example.dogapp.feature.donate.presentation.CharitiesViewModel
import com.example.dogapp.feature.donate.presentation.CharityViewModel
import com.example.dogapp.feature.donate.presentation.DonateViewModel
import com.example.dogapp.feature.donate.presentation.DonationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val donateModule =
    module {
        single { MockCharityRepository() } bind CharityRepository::class
        single<DonationRepository> { DefaultDonationRepository(dao = get()) }
        single<PaymentProcessor> { DemoPaymentProcessor() }
        factory { DonateUseCase(paymentCards = get(), processor = get(), repository = get()) }
        viewModelOf(::CharitiesViewModel)
        viewModelOf(::CharityViewModel)
        viewModelOf(::DonateViewModel)
        viewModel { DonationsViewModel(repository = get()) }
    }
