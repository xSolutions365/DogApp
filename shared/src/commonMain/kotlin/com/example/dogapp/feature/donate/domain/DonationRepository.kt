package com.example.dogapp.feature.donate.domain

import kotlinx.coroutines.flow.Flow

interface DonationRepository {
    fun observeDonations(): Flow<List<Donation>>

    suspend fun record(donation: Donation)
}
