package com.example.dogapp.feature.wallet.presentation

sealed interface AddCardEvent {
    data object Close : AddCardEvent
}
