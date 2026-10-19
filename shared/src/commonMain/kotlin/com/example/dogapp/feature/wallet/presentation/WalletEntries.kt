package com.example.dogapp.feature.wallet.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.walletEntries(
    layout: WalletLayout,
    onBack: () -> Unit,
) {
    entry<WalletRoute> { WalletRoot(layout = layout, onBack = onBack) }
}
