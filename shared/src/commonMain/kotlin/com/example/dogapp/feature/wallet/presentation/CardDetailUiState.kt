package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.domain.SavedCard

data class CardDetailUiState(
    val card: SavedCard?,
    val isRemoving: Boolean = false,
)
