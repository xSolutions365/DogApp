package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardFieldError

data class AddCardUiState(
    val number: String = "",
    val expiry: String = "",
    val securityCode: String = "",
    val brand: CardBrand? = null,
    val errors: Set<CardFieldError> = emptySet(),
    val isSaving: Boolean = false,
)
