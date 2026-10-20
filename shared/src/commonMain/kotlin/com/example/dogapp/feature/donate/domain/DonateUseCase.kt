package com.example.dogapp.feature.donate.domain

import com.example.dogapp.core.domain.Outcome
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DonateUseCase(
    private val paymentCards: PaymentCards,
    private val processor: PaymentProcessor,
    private val repository: DonationRepository,
    private val clock: Clock = Clock.System,
    private val newDonationId: () -> String = { Uuid.random().toString() },
) {
    suspend operator fun invoke(
        charity: Charity,
        amount: DonationAmount,
        method: PaymentMethod,
    ): Outcome<Donation, PaymentError> {
        val paidWith =
            when (method) {
                is PaymentMethod.Card -> {
                    when (val confirmed = paymentCards.confirm(method.card.id)) {
                        is Outcome.Failure -> return confirmed
                        is Outcome.Success -> PaidWith.Card(brandName = confirmed.data.brandName, last4 = confirmed.data.last4)
                    }
                }

                is PaymentMethod.GooglePay -> {
                    PaidWith.GooglePay(method.cardDescription)
                }
            }
        return when (val charged = processor.charge(amount, method)) {
            is Outcome.Failure -> {
                charged
            }

            is Outcome.Success -> {
                val donation =
                    Donation(
                        id = newDonationId(),
                        charityId = charity.id,
                        charityName = charity.name,
                        amount = amount,
                        paidWith = paidWith,
                        donatedAt = Instant.fromEpochMilliseconds(clock.now().toEpochMilliseconds()),
                    )
                repository.record(donation)
                Outcome.Success(donation)
            }
        }
    }
}
