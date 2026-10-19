package com.example.dogapp.feature.wallet.presentation

sealed interface WalletAction {
    data object Unlock : WalletAction

    data object SetUpAgain : WalletAction

    data object ScreenResume : WalletAction

    data class RemoveCard(
        val cardId: String,
    ) : WalletAction
}
