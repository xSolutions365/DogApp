package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.data.DemoPaymentProcessor
import com.example.dogapp.feature.donate.domain.DonateUseCase
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaidWith
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.donate.domain.PaymentMethod
import com.example.dogapp.testing.FakeCharityRepository
import com.example.dogapp.testing.FakeDonationRepository
import com.example.dogapp.testing.FakePaymentCards
import com.example.dogapp.testing.testCharity
import com.example.dogapp.testing.testPaymentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DonateViewModelTest {
    private val visa = testPaymentCard("4242")
    private val declining = testPaymentCard("0002")
    private val donations = FakeDonationRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.open(paymentCards: FakePaymentCards): Pair<DonateViewModel, List<DonateEvent>> {
        val viewModel =
            DonateViewModel(
                charityId = "fetch-club",
                charities = FakeCharityRepository(listOf(testCharity("fetch-club", "Fetch Club"))),
                paymentCards = paymentCards,
                donate = DonateUseCase(paymentCards, DemoPaymentProcessor(), donations),
            )
        val events = mutableListOf<DonateEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { events += it } }
        return viewModel to events
    }

    @Test
    fun sheetStartsAtFivePoundsAndALockedWalletIsUnlockedToListTheCards() =
        runTest {
            val (viewModel, _) = open(FakePaymentCards(listOf(visa, declining), unlocked = false))

            assertEquals("Fetch Club", viewModel.state.value.charityName)
            assertEquals(DonationAmount(500), viewModel.state.value.amount)
            assertNull(viewModel.state.value.cards)

            viewModel.onAction(DonateAction.UnlockWallet)

            assertEquals(listOf(visa, declining), viewModel.state.value.cards)
            assertEquals(visa.id, viewModel.state.value.selectedCardId)
        }

    @Test
    fun otherAmountMustBeOneToFiveHundredPoundsAndThenTheDonationIsRecorded() =
        runTest {
            val (viewModel, events) = open(FakePaymentCards(listOf(visa)))
            viewModel.onAction(DonateAction.SelectOtherAmount)
            viewModel.onAction(DonateAction.OtherAmountChange("600"))
            viewModel.onAction(DonateAction.DonateWithCard)

            assertTrue(viewModel.state.value.showAmountError)
            assertEquals(emptyList(), donations.donations.value)

            viewModel.onAction(DonateAction.OtherAmountChange("7.50"))
            viewModel.onAction(DonateAction.DonateWithCard)

            val donation = donations.donations.value.single()
            assertEquals(DonationAmount(750), donation.amount)
            assertEquals(PaidWith.Card(brandName = "Visa", last4 = "4242"), donation.paidWith)
            assertEquals(donation, viewModel.state.value.completed)
            assertEquals(listOf<DonateEvent>(DonateEvent.Donated(donation)), events)
        }

    @Test
    fun aDeclinedCardIsExplainedAndACancelledPromptIsNot() =
        runTest {
            val paymentCards = FakePaymentCards(listOf(visa, declining))
            val (viewModel, events) = open(paymentCards)

            viewModel.onAction(DonateAction.SelectCard(declining.id))
            viewModel.onAction(DonateAction.DonateWithCard)
            assertEquals(PaymentError.Declined, viewModel.state.value.error)

            viewModel.onAction(DonateAction.SelectCard(visa.id))
            assertNull(viewModel.state.value.error)
            paymentCards.nextFailure = PaymentError.Cancelled
            viewModel.onAction(DonateAction.DonateWithCard)

            assertNull(viewModel.state.value.error)
            assertEquals(false, viewModel.state.value.isDonating)
            assertEquals(emptyList(), events)
            assertIs<List<*>>(donations.donations.value)
            assertEquals(emptyList(), donations.donations.value)
        }

    @Test
    fun googlePayOpensForAValidAmountWithoutTheWalletAndRecordsTheAmountItShowed() =
        runTest {
            val (viewModel, events) = open(FakePaymentCards(listOf(visa), unlocked = false))
            viewModel.onAction(DonateAction.SelectOtherAmount)
            viewModel.onAction(DonateAction.OtherAmountChange("0.50"))
            viewModel.onAction(DonateAction.StartGooglePay)

            assertTrue(viewModel.state.value.showAmountError)
            assertEquals(emptyList(), events)

            viewModel.onAction(DonateAction.OtherAmountChange("12"))
            viewModel.onAction(DonateAction.StartGooglePay)
            viewModel.onAction(DonateAction.SelectPreset(DonationAmount(2000)))
            viewModel.onAction(DonateAction.DonateWithGooglePay(PaymentMethod.GooglePay(token = null, cardDescription = null)))

            val donation = donations.donations.value.single()
            assertEquals(DonationAmount(1200), donation.amount)
            assertEquals(PaidWith.GooglePay(cardDescription = null), donation.paidWith)
            assertNull(viewModel.state.value.cards)
            assertEquals(
                listOf(DonateEvent.LaunchGooglePay(DonationAmount(1200)), DonateEvent.Donated(donation)),
                events,
            )
        }
}
