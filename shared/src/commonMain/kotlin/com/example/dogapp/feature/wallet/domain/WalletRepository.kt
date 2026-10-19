package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.Outcome

interface WalletRepository {
    suspend fun unlock(): Outcome<Wallet, WalletError>

    suspend fun wipe()
}
