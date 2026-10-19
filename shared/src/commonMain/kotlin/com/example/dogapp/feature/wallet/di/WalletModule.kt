package com.example.dogapp.feature.wallet.di

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.feature.wallet.domain.ValidateCardUseCase
import com.example.dogapp.feature.wallet.domain.WalletAutoLock
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletRepository
import com.example.dogapp.feature.wallet.presentation.AddCardViewModel
import com.example.dogapp.feature.wallet.presentation.CardDetailViewModel
import com.example.dogapp.feature.wallet.presentation.WalletViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val walletModule =
    module {
        single<WalletRepository> { DefaultWalletRepository(vault = get()) }
        single { WalletCoordinator(repository = get()) }
        single { WalletAutoLock(coordinator = get()) }
        factoryOf(::ValidateCardUseCase)
        viewModel { WalletViewModel(coordinator = get()) }
        viewModelOf(::AddCardViewModel)
        viewModelOf(::CardDetailViewModel)
    }
