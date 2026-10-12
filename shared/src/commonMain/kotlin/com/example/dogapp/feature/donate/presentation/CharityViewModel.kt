package com.example.dogapp.feature.donate.presentation

import androidx.lifecycle.ViewModel
import com.example.dogapp.feature.donate.domain.CharityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharityViewModel(
    charityId: String,
    repository: CharityRepository,
) : ViewModel() {
    val state: StateFlow<CharityUiState> =
        MutableStateFlow(repository.charity(charityId)?.let { CharityUiState.Loaded(it) } ?: CharityUiState.NotFound).asStateFlow()
}
