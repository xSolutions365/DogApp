package com.example.dogapp.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardValidation
import com.example.dogapp.feature.wallet.domain.ValidateCardUseCase
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddCardViewModel(
    private val validateCard: ValidateCardUseCase,
    private val coordinator: WalletCoordinator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AddCardUiState())
    private val eventChannel = Channel<AddCardEvent>()

    val state: StateFlow<AddCardUiState> = mutableState.asStateFlow()
    val events: Flow<AddCardEvent> = eventChannel.receiveAsFlow()

    fun onAction(action: AddCardAction) {
        when (action) {
            is AddCardAction.NumberChange -> {
                val digits = action.number.filterNot { it == ' ' || it == '-' }
                mutableState.update { it.copy(number = action.number, brand = digits.takeIf(String::isNotEmpty)?.let(CardBrand::of)) }
            }

            is AddCardAction.ExpiryChange -> {
                mutableState.update { it.copy(expiry = action.expiry) }
            }

            is AddCardAction.SecurityCodeChange -> {
                mutableState.update { it.copy(securityCode = action.securityCode) }
            }

            AddCardAction.Save -> {
                save()
            }
        }
    }

    private fun save() {
        val current = mutableState.value
        if (current.isSaving) return
        when (val validation = validateCard(current.number, current.expiry, current.securityCode)) {
            is CardValidation.Invalid -> {
                mutableState.update { it.copy(errors = validation.errors) }
            }

            is CardValidation.Valid -> {
                mutableState.update { it.copy(errors = emptySet(), isSaving = true) }
                viewModelScope.launch {
                    val outcome = coordinator.addCard(validation)
                    if (outcome is Outcome.Success || outcome.closesTheForm()) {
                        mutableState.value = AddCardUiState()
                        eventChannel.send(AddCardEvent.Close)
                    } else {
                        mutableState.update { it.copy(isSaving = false) }
                    }
                }
            }
        }
    }

    private fun Outcome<*, WalletError>.closesTheForm(): Boolean =
        this is Outcome.Failure && (error == WalletError.Locked || error == WalletError.KeyInvalidated)
}
