package com.example.dogapp.app.di

import com.example.dogapp.feature.donate.domain.PaymentCards
import org.koin.dsl.module

val paymentModule =
    module {
        single<PaymentCards> { WalletPaymentCards(coordinator = get(), scope = get()) }
    }
