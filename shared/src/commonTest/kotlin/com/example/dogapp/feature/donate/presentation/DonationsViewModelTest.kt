package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaidWith
import com.example.dogapp.testing.FakeDonationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DonationsViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun historyFollowsTheRecordedDonationsNewestFirstDatedInTheLocalTimeZone() =
        runTest {
            val repository = FakeDonationRepository()
            val viewModel = DonationsViewModel(repository) { TimeZone.of("Europe/London") }
            assertNull(viewModel.state.value.donations)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

            assertEquals(emptyList(), viewModel.state.value.donations)

            repository.record(donation("first", "2026-10-19T09:00:00Z", PaidWith.Card("Visa", "4242")))
            repository.record(donation("late-evening", "2026-10-19T23:30:00Z", PaidWith.GooglePay("Visa •••• 1111")))

            assertEquals(
                listOf(
                    DonationListItem(
                        "late-evening",
                        "Fetch Club",
                        DonationAmount(750),
                        PaidWith.GooglePay("Visa •••• 1111"),
                        "20 Oct 2026",
                    ),
                    DonationListItem("first", "Fetch Club", DonationAmount(750), PaidWith.Card("Visa", "4242"), "19 Oct 2026"),
                ),
                viewModel.state.value.donations,
            )
        }

    private fun donation(
        id: String,
        at: String,
        paidWith: PaidWith,
    ) = Donation(
        id = id,
        charityId = "fetch-club",
        charityName = "Fetch Club",
        amount = DonationAmount(750),
        paidWith = paidWith,
        donatedAt = Instant.parse(at),
    )
}
