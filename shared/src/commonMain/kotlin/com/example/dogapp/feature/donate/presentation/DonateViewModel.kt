package com.example.dogapp.feature.donate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.CharityRepository
import com.example.dogapp.feature.donate.domain.DonateUseCase
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentCards
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.donate.domain.PaymentMethod
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonateViewModel(
    charityId: String,
    charities: CharityRepository,
    private val paymentCards: PaymentCards,
    private val donate: DonateUseCase,
) : ViewModel() {
    private val charity = checkNotNull(charities.charity(charityId)) { "Unknown charity $charityId" }
    private val mutableState = MutableStateFlow(DonateUiState(charityName = charity.name))
    private val eventChannel = Channel<DonateEvent>()
    private var googlePayAmount: DonationAmount? = null

    val state: StateFlow<DonateUiState> = mutableState.asStateFlow()
    val events: Flow<DonateEvent> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            paymentCards.cards.collect { cards ->
                mutableState.update { state ->
                    val selected = state.selectedCardId?.takeIf { id -> cards?.any { it.id == id } == true } ?: cards?.firstOrNull()?.id
                    state.copy(cards = cards, selectedCardId = selected)
                }
            }
        }
    }

    fun onAction(action: DonateAction) {
        when (action) {
            is DonateAction.SelectPreset -> {
                mutableState.update { it.copy(selectedPreset = action.amount, showAmountError = false) }
            }

            DonateAction.SelectOtherAmount -> {
                mutableState.update { it.copy(selectedPreset = null) }
            }

            is DonateAction.OtherAmountChange -> {
                mutableState.update { it.copy(otherAmount = action.text, showAmountError = false) }
            }

            is DonateAction.SelectCard -> {
                mutableState.update { it.copy(selectedCardId = action.cardId, error = null) }
            }

            DonateAction.UnlockWallet -> {
                viewModelScope.launch { paymentCards.unlock().showError() }
            }

            DonateAction.DonateWithCard -> {
                donateWithCard()
            }

            DonateAction.StartGooglePay -> {
                startGooglePay()
            }

            is DonateAction.DonateWithGooglePay -> {
                googlePayAmount?.let { amount -> donate(amount, action.payment) }
                googlePayAmount = null
            }
        }
    }

    private fun donateWithCard() {
        val current = mutableState.value
        if (current.isDonating) return
        val amount = current.amount ?: return mutableState.update { it.copy(showAmountError = true) }
        val card = current.cards?.firstOrNull { it.id == current.selectedCardId } ?: return
        donate(amount, PaymentMethod.Card(card))
    }

    private fun startGooglePay() {
        val current = mutableState.value
        if (current.isDonating) return
        val amount = current.amount ?: return mutableState.update { it.copy(showAmountError = true) }
        googlePayAmount = amount
        mutableState.update { it.copy(error = null) }
        viewModelScope.launch { eventChannel.send(DonateEvent.LaunchGooglePay(amount)) }
    }

    private fun donate(
        amount: DonationAmount,
        method: PaymentMethod,
    ) {
        if (mutableState.value.isDonating) return
        mutableState.update { it.copy(isDonating = true, error = null) }
        viewModelScope.launch {
            when (val outcome = donate(charity, amount, method)) {
                is Outcome.Success -> {
                    mutableState.update { it.copy(isDonating = false, completed = outcome.data) }
                    eventChannel.send(DonateEvent.Donated(outcome.data))
                }

                is Outcome.Failure -> {
                    mutableState.update { it.copy(isDonating = false) }
                    outcome.showError()
                }
            }
        }
    }

    private fun Outcome<*, PaymentError>.showError() {
        val error = (this as? Outcome.Failure)?.error?.takeUnless { it == PaymentError.Cancelled }
        mutableState.update { it.copy(error = error) }
    }
}
