package com.example.dogapp.feature.profile.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.profileEntries(onBreedClick: (String) -> Unit) {
    entry<ProfileRoute> { ProfileRoot(onBreedClick = onBreedClick) }
}
