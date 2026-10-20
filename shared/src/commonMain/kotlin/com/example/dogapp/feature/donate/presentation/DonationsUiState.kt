package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaidWith

data class DonationsUiState(
    val donations: List<DonationListItem>? = null,
)

data class DonationListItem(
    val id: String,
    val charityName: String,
    val amount: DonationAmount,
    val paidWith: PaidWith,
    val date: String,
)
