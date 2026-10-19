package com.example.dogapp.feature.wallet.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class CardDetailRoute(
    val cardId: String,
) : NavKey
