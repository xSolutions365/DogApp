package com.example.dogapp.feature.donate.presentation

import androidx.lifecycle.ViewModel
import com.example.dogapp.feature.donate.domain.CharityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharitiesViewModel(
    repository: CharityRepository,
) : ViewModel() {
    val state: StateFlow<CharitiesUiState> = MutableStateFlow(CharitiesUiState(repository.charities())).asStateFlow()
}
