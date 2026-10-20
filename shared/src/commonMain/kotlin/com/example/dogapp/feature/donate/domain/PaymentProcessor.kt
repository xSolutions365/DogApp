package com.example.dogapp.feature.donate.domain

import com.example.dogapp.core.domain.Outcome

interface PaymentProcessor {
    suspend fun charge(
        amount: DonationAmount,
        method: PaymentMethod,
    ): Outcome<Unit, PaymentError>
}
