package com.example.dogapp.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.feature.wallet.domain.SavedCard
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardDetailViewModel(
    private val cardId: String,
    private val coordinator: WalletCoordinator,
) : ViewModel() {
    private val isRemoving = MutableStateFlow(false)
    private val eventChannel = Channel<CardDetailEvent>()

    val state: StateFlow<CardDetailUiState> =
        combine(coordinator.status, isRemoving) { status, removing -> CardDetailUiState(card = status.card(), isRemoving = removing) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                CardDetailUiState(coordinator.status.value.card()),
            )
    val events: Flow<CardDetailEvent> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            coordinator.status
                .map { status -> status.card() == null }
                .distinctUntilChanged()
                .filter { isGone -> isGone }
                .collect { eventChannel.send(CardDetailEvent.Close) }
        }
    }

    fun onAction(action: CardDetailAction) {
        when (action) {
            CardDetailAction.Remove -> remove()
        }
    }

    private fun remove() {
        if (isRemoving.value) return
        isRemoving.value = true
        viewModelScope.launch {
            coordinator.removeCard(cardId)
            isRemoving.value = false
        }
    }

    private fun WalletStatus.card(): SavedCard? = (this as? WalletStatus.Unlocked)?.wallet?.cards?.firstOrNull { it.id == cardId }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
