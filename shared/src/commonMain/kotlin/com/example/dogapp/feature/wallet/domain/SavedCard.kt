package com.example.dogapp.feature.wallet.domain

data class SavedCard(
    val id: String,
    val brand: CardBrand,
    val last4: String,
    val expiry: CardExpiry,
)
