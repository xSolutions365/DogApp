package com.example.dogapp.feature.home.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.homeEntries(
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
) {
    entry<HomeRoute> { HomeRoot(onBreedClick = onBreedClick, onIdentifyClick = onIdentifyClick) }
}
