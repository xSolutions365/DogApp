package com.example.dogapp.feature.donate.domain

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.data.DemoPaymentProcessor
import com.example.dogapp.testing.FakeDonationRepository
import com.example.dogapp.testing.FakePaymentCards
import com.example.dogapp.testing.testCharity
import com.example.dogapp.testing.testPaymentCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class DonateUseCaseTest {
    private val now = Instant.parse("2026-10-19T12:00:00Z")
    private val visa = testPaymentCard("4242")
    private val declining = testPaymentCard("0002")
    private val paymentCards = FakePaymentCards(listOf(visa, declining))
    private val repository = FakeDonationRepository()
    private val charity = testCharity("fetch-club", "Fetch Club")
    private val donate =
        DonateUseCase(
            paymentCards = paymentCards,
            processor = DemoPaymentProcessor(),
            repository = repository,
            clock =
                object : Clock {
                    override fun now(): Instant = now
                },
            newDonationId = { "donation-1" },
        )

    @Test
    fun cardDonationIsConfirmedChargedAndRecorded() =
        runTest {
            val expected =
                Donation(
                    id = "donation-1",
                    charityId = "fetch-club",
                    charityName = "Fetch Club",
                    amount = DonationAmount(500),
                    paidWith = PaidWith.Card(brandName = "Visa", last4 = "4242"),
                    donatedAt = now,
                )

            assertEquals(Outcome.Success(expected), donate(charity, DonationAmount(500), PaymentMethod.Card(visa)))
            assertEquals(listOf("card-4242"), paymentCards.confirmed)
            assertEquals(listOf(expected), repository.observeDonations().first())
        }

    @Test
    fun googlePayDonationNeedsNoWalletConfirmation() =
        runTest {
            val outcome = donate(charity, DonationAmount(1000), PaymentMethod.GooglePay(token = "{}", cardDescription = "Visa •••• 1111"))

            assertEquals(PaidWith.GooglePay("Visa •••• 1111"), (outcome as Outcome.Success).data.paidWith)
            assertEquals(emptyList(), paymentCards.confirmed)
        }

    @Test
    fun cancelledConfirmationOrADeclinedCardRecordsNothing() =
        runTest {
            paymentCards.nextFailure = PaymentError.Cancelled
            assertEquals(Outcome.Failure(PaymentError.Cancelled), donate(charity, DonationAmount(500), PaymentMethod.Card(visa)))

            assertEquals(Outcome.Failure(PaymentError.Declined), donate(charity, DonationAmount(500), PaymentMethod.Card(declining)))

            assertEquals(emptyList(), repository.observeDonations().first())
        }
}
