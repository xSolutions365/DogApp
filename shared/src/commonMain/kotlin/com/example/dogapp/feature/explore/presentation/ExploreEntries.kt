package com.example.dogapp.feature.explore.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.exploreEntries(onBreedClick: (String) -> Unit) {
    entry<ExploreRoute> { ExploreRoot(onBreedClick = onBreedClick) }
}
