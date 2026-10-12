package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.Charity

sealed interface CharityUiState {
    data object NotFound : CharityUiState

    data class Loaded(
        val charity: Charity,
    ) : CharityUiState
}
