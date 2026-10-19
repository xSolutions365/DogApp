package com.example.dogapp.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.onFailure
import com.example.dogapp.core.domain.onSuccess
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletError
import com.example.dogapp.feature.wallet.domain.WalletStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalletViewModel(
    private val coordinator: WalletCoordinator,
) : ViewModel() {
    private val biometricUnavailable = MutableStateFlow(false)
    private var isUnlocking = false

    val state: StateFlow<WalletUiState> =
        combine(coordinator.status, biometricUnavailable) { status, unavailable -> status.toUiState(unavailable) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), coordinator.status.value.toUiState(false))

    init {
        if (coordinator.status.value == WalletStatus.Locked) unlock()
    }

    fun onAction(action: WalletAction) {
        when (action) {
            WalletAction.Unlock, WalletAction.SetUpAgain -> unlock()
            WalletAction.ScreenResume -> if (biometricUnavailable.value) unlock()
            is WalletAction.RemoveCard -> viewModelScope.launch { coordinator.removeCard(action.cardId) }
        }
    }

    private fun unlock() {
        if (isUnlocking) return
        isUnlocking = true
        viewModelScope.launch {
            coordinator
                .unlock()
                .onSuccess { biometricUnavailable.value = false }
                .onFailure { error -> biometricUnavailable.value = error == WalletError.BiometricUnavailable }
            isUnlocking = false
        }
    }

    private fun WalletStatus.toUiState(unavailable: Boolean): WalletUiState =
        when (this) {
            is WalletStatus.Unlocked -> WalletUiState.Unlocked(wallet.cards)
            WalletStatus.KeyInvalidated -> WalletUiState.KeyInvalidated
            WalletStatus.Locked -> if (unavailable) WalletUiState.BiometricUnavailable else WalletUiState.Locked
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
