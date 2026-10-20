package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentCard
import com.example.dogapp.feature.donate.domain.PaymentError

data class DonateUiState(
    val charityName: String = "",
    val selectedPreset: DonationAmount? = DonationAmount.presets[1],
    val otherAmount: String = "",
    val showAmountError: Boolean = false,
    val cards: List<PaymentCard>? = null,
    val selectedCardId: String? = null,
    val isDonating: Boolean = false,
    val error: PaymentError? = null,
    val completed: Donation? = null,
) {
    val amount: DonationAmount?
        get() = selectedPreset ?: DonationAmount.fromPounds(otherAmount)
}
