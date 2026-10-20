package com.example.dogapp.feature.profile.presentation

import com.example.dogapp.core.domain.security.IntegrityCheck

data class DeviceStateUiState(
    val checks: List<IntegrityCheck>? = null,
)
