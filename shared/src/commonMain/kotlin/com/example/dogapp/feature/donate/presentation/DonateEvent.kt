package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount

sealed interface DonateEvent {
    data class Donated(
        val donation: Donation,
    ) : DonateEvent

    data class LaunchGooglePay(
        val amount: DonationAmount,
    ) : DonateEvent
}
