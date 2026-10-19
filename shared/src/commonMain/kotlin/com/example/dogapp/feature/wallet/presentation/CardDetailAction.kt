package com.example.dogapp.feature.wallet.presentation

sealed interface CardDetailAction {
    data object Remove : CardDetailAction
}
