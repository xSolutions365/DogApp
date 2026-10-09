package com.example.dogapp.feature.identify.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.identifyEntries(
    onBreedClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    entry<IdentifyRoute> { IdentifyRoot(onBreedClick = onBreedClick, onBack = onBack) }
}
