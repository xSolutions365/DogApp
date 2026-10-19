package com.example.dogapp.feature.wallet.presentation

sealed interface AddCardAction {
    data class NumberChange(
        val number: String,
    ) : AddCardAction

    data class ExpiryChange(
        val expiry: String,
    ) : AddCardAction

    data class SecurityCodeChange(
        val securityCode: String,
    ) : AddCardAction

    data object Save : AddCardAction
}
