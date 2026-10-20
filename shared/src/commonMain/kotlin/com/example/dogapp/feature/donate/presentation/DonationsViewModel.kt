package com.example.dogapp.feature.donate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.presentation.toDisplayText
import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DonationsViewModel(
    repository: DonationRepository,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : ViewModel() {
    val state: StateFlow<DonationsUiState> =
        repository
            .observeDonations()
            .map { donations -> DonationsUiState(donations = donations.map { it.toListItem() }) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DonationsUiState())

    private fun Donation.toListItem(): DonationListItem =
        DonationListItem(
            id = id,
            charityName = charityName,
            amount = amount,
            paidWith = paidWith,
            date = donatedAt.toLocalDateTime(timeZone()).date.toDisplayText(),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
