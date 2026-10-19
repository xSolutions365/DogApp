package com.example.dogapp.feature.wallet.domain

sealed interface CardValidation {
    data class Valid(
        val brand: CardBrand,
        val last4: String,
        val expiry: CardExpiry,
    ) : CardValidation

    data class Invalid(
        val errors: Set<CardFieldError>,
    ) : CardValidation
}

enum class CardFieldError {
    InvalidNumber,
    InvalidExpiry,
    ExpiredCard,
    InvalidSecurityCode,
}
