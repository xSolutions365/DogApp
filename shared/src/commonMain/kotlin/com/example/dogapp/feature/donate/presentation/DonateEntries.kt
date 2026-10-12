package com.example.dogapp.feature.donate.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.donateEntries(
    onCharityClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    entry<CharitiesRoute> { CharitiesRoot(onCharityClick = onCharityClick, onBack = onBack) }
    entry<CharityRoute> { route -> CharityRoot(charityId = route.charityId, onBack = onBack) }
}
