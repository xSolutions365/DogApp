package com.example.dogapp.feature.wallet.domain

sealed interface WalletStatus {
    data object Locked : WalletStatus

    data class Unlocked(
        val wallet: Wallet,
    ) : WalletStatus

    data object KeyInvalidated : WalletStatus
}
