package com.example.dogapp.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class DeviceStateViewModel(
    integrityChecker: DeviceIntegrityChecker,
) : ViewModel() {
    val state: StateFlow<DeviceStateUiState> =
        flow { emit(DeviceStateUiState(checks = integrityChecker.check().checks)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DeviceStateUiState())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
