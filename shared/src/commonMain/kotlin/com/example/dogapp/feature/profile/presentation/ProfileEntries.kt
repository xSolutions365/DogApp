package com.example.dogapp.feature.profile.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.profileEntries(
    onBreedClick: (String) -> Unit,
    onSupportCharitiesClick: () -> Unit,
    onDonationsClick: () -> Unit,
    onWalletClick: () -> Unit,
    onDeviceChecksClick: () -> Unit,
    onBack: () -> Unit,
) {
    entry<ProfileRoute> {
        ProfileRoot(
            onBreedClick = onBreedClick,
            onSupportCharitiesClick = onSupportCharitiesClick,
            onDonationsClick = onDonationsClick,
            onWalletClick = onWalletClick,
            onDeviceChecksClick = onDeviceChecksClick,
        )
    }
    entry<DeviceChecksRoute> { DeviceChecksRoot(onBack = onBack) }
}
