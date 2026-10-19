package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.DomainError

sealed interface WalletError : DomainError {
    data object AuthenticationCancelled : WalletError

    data object AuthenticationFailed : WalletError

    data object BiometricUnavailable : WalletError

    data object KeyInvalidated : WalletError

    data object Locked : WalletError
}
