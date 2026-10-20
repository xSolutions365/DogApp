package com.example.dogapp.feature.donate.domain

import com.example.dogapp.core.domain.DomainError

sealed interface PaymentError : DomainError {
    data object Cancelled : PaymentError

    data object AuthenticationFailed : PaymentError

    data object WalletUnavailable : PaymentError

    data object CardNotFound : PaymentError

    data object Declined : PaymentError

    data object GooglePayFailed : PaymentError
}
