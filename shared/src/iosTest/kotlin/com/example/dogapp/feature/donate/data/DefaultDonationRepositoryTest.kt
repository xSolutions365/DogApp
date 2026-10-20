package com.example.dogapp.feature.donate.data

import androidx.room.Room
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.database.buildDogAppDatabase
import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaidWith
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DefaultDonationRepositoryTest {
    private val database: DogAppDatabase = buildDogAppDatabase(Room.inMemoryDatabaseBuilder<DogAppDatabase>())
    private val repository = DefaultDonationRepository(database.donationDao())

    @AfterTest
    fun tearDown() {
        database.close()
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

    @Test
    fun donationsPaidEitherWayAreKeptAndObservedNewestFirst() =
        runBlocking {
            val byCard = donation("one", "2026-10-19T09:00:00Z", PaidWith.Card(brandName = "Visa", last4 = "4242"))
            val byGooglePay = donation("two", "2026-10-19T10:00:00Z", PaidWith.GooglePay("Visa •••• 1111"))
            val byGooglePayWithoutDetails = donation("three", "2026-10-19T11:00:00Z", PaidWith.GooglePay(null))

            repository.record(byCard)
            repository.record(byGooglePay)
            repository.record(byGooglePayWithoutDetails)

            assertEquals(listOf(byGooglePayWithoutDetails, byGooglePay, byCard), repository.observeDonations().first())
        }
}
