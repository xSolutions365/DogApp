package com.example.dogapp.feature.wallet.presentation

import kotlinx.datetime.LocalDate

sealed interface WalletUiState {
    data object Locked : WalletUiState

    data class Unlocked(
        val createdOn: LocalDate,
    ) : WalletUiState

    data object KeyInvalidated : WalletUiState

    data object BiometricUnavailable : WalletUiState
}
