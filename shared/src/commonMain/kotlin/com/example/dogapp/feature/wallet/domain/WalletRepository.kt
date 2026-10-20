package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.Outcome

interface WalletRepository {
    suspend fun unlock(access: WalletAccess): Outcome<Wallet, WalletError>

    suspend fun save(
        wallet: Wallet,
        change: WalletChange,
    ): Outcome<Unit, WalletError>

    suspend fun wipe()
}
