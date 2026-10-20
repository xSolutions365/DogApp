package com.example.dogapp.feature.donate.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.donateEntries(
    onCharityClick: (String) -> Unit,
    onDonateClick: (String) -> Unit,
    onSeeCharitiesClick: () -> Unit,
    onBack: () -> Unit,
) {
    entry<CharitiesRoute> { CharitiesRoot(onCharityClick = onCharityClick, onBack = onBack) }
    entry<CharityRoute> { route -> CharityRoot(charityId = route.charityId, onDonateClick = onDonateClick, onBack = onBack) }
    entry<DonationsRoute> { DonationsRoot(onSeeCharitiesClick = onSeeCharitiesClick, onBack = onBack) }
}
