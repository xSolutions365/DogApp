package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentMethod

sealed interface DonateAction {
    data class SelectPreset(
        val amount: DonationAmount,
    ) : DonateAction

    data object SelectOtherAmount : DonateAction

    data class OtherAmountChange(
        val text: String,
    ) : DonateAction

    data class SelectCard(
        val cardId: String,
    ) : DonateAction

    data object UnlockWallet : DonateAction

    data object DonateWithCard : DonateAction

    data object StartGooglePay : DonateAction

    data class DonateWithGooglePay(
        val payment: PaymentMethod.GooglePay,
    ) : DonateAction
}
