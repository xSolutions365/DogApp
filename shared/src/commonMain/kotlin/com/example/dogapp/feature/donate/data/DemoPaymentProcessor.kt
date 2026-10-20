package com.example.dogapp.feature.donate.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.donate.domain.PaymentMethod
import com.example.dogapp.feature.donate.domain.PaymentProcessor

class DemoPaymentProcessor : PaymentProcessor {
    override suspend fun charge(
        amount: DonationAmount,
        method: PaymentMethod,
    ): Outcome<Unit, PaymentError> =
        when {
            amount !in DonationAmount.minimum..DonationAmount.maximum -> Outcome.Failure(PaymentError.Declined)
            method is PaymentMethod.Card && method.card.last4 == DECLINED_TEST_CARD_LAST4 -> Outcome.Failure(PaymentError.Declined)
            else -> Outcome.Success(Unit)
        }

    private companion object {
        const val DECLINED_TEST_CARD_LAST4 = "0002"
    }
}
