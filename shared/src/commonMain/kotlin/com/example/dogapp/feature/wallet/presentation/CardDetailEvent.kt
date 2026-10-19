package com.example.dogapp.feature.wallet.presentation

sealed interface CardDetailEvent {
    data object Close : CardDetailEvent
}
