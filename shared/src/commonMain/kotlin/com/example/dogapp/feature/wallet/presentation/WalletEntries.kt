package com.example.dogapp.feature.wallet.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.walletEntries(
    layout: WalletLayout,
    onAddCardClick: () -> Unit,
    onCardClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    entry<WalletRoute> { WalletRoot(layout = layout, onAddCardClick = onAddCardClick, onCardClick = onCardClick, onBack = onBack) }
    entry<CardDetailRoute> { route -> CardDetailRoot(cardId = route.cardId, onBack = onBack) }
    entry<AddCardRoute> { AddCardRoot(presentation = AddCardPresentation.Screen, onDismiss = onBack) }
}
