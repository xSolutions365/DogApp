package com.example.dogapp.feature.wallet.di

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletRepository
import com.example.dogapp.feature.wallet.presentation.WalletViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val walletModule =
    module {
        single<WalletRepository> { DefaultWalletRepository(vault = get()) }
        singleOf(::WalletCoordinator)
        viewModel { WalletViewModel(coordinator = get()) }
    }
