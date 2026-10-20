package com.example.dogapp.feature.profile.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.profileEntries(
    layout: ProfileLayout,
    onBreedClick: (String) -> Unit,
    onSeeAllFavouritesClick: () -> Unit,
    onSeeAllDrawsClick: () -> Unit,
    onSupportCharitiesClick: () -> Unit,
    onDonationsClick: () -> Unit,
    onWalletClick: () -> Unit,
    onDeviceStateClick: () -> Unit,
    onBack: () -> Unit,
) {
    entry<ProfileRoute> {
        ProfileRoot(
            layout = layout,
            onBreedClick = onBreedClick,
            onSeeAllFavouritesClick = onSeeAllFavouritesClick,
            onSeeAllDrawsClick = onSeeAllDrawsClick,
            onSupportCharitiesClick = onSupportCharitiesClick,
            onDonationsClick = onDonationsClick,
            onWalletClick = onWalletClick,
            onDeviceStateClick = onDeviceStateClick,
        )
    }
    entry<FavouritesRoute> { FavouritesRoot(onBreedClick = onBreedClick, onBack = onBack) }
    entry<DrawHistoryRoute> { DrawHistoryRoot(onBreedClick = onBreedClick, onBack = onBack) }
    entry<DeviceStateRoute> { DeviceStateRoot(onBack = onBack) }
}
