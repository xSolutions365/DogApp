package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.domain.SavedCard

sealed interface WalletUiState {
    data object Locked : WalletUiState

    data class Unlocked(
        val cards: List<SavedCard>,
    ) : WalletUiState

    data object KeyInvalidated : WalletUiState

    data object BiometricUnavailable : WalletUiState
}
