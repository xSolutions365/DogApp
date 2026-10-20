package com.example.dogapp.feature.donate.domain

import kotlin.time.Instant

data class Donation(
    val id: String,
    val charityId: String,
    val charityName: String,
    val amount: DonationAmount,
    val paidWith: PaidWith,
    val donatedAt: Instant,
)

sealed interface PaidWith {
    data class Card(
        val brandName: String,
        val last4: String,
    ) : PaidWith

    data class GooglePay(
        val cardDescription: String?,
    ) : PaidWith
}
