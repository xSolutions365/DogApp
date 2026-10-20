package com.example.dogapp.testing

import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDonationRepository : DonationRepository {
    val donations = MutableStateFlow<List<Donation>>(emptyList())

    override fun observeDonations(): Flow<List<Donation>> = donations

    override suspend fun record(donation: Donation) {
        donations.value = listOf(donation) + donations.value
    }
}
