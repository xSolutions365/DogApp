package com.example.dogapp.feature.breeddetail.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.breedDetailEntries(onBack: () -> Unit) {
    entry<BreedDetailRoute> { route -> BreedDetailRoot(breedId = route.breedId, onBack = onBack) }
}
