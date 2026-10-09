package com.example.dogapp.feature.home.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.homeEntries(
    layout: HomeLayout,
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
) {
    entry<HomeRoute> { HomeRoot(layout = layout, onBreedClick = onBreedClick, onIdentifyClick = onIdentifyClick) }
}
