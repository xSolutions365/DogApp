package com.example.dogapp.feature.profile.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.profileEntries(
    onBreedClick: (String) -> Unit,
    onSupportCharitiesClick: () -> Unit,
    onWalletClick: () -> Unit,
) {
    entry<ProfileRoute> {
        ProfileRoot(onBreedClick = onBreedClick, onSupportCharitiesClick = onSupportCharitiesClick, onWalletClick = onWalletClick)
    }
}
