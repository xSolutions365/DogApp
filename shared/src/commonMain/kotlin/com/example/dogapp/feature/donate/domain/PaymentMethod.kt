package com.example.dogapp.feature.donate.domain

sealed interface PaymentMethod {
    data class Card(
        val card: PaymentCard,
    ) : PaymentMethod

    data class GooglePay(
        val token: String?,
        val cardDescription: String?,
    ) : PaymentMethod
}
