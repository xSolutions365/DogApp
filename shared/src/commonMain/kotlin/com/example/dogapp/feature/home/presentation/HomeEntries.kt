package com.example.dogapp.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.homeEntries(
    layout: HomeLayout,
    onBreedClick: (String) -> Unit,
    onTakePhotoClick: () -> Unit,
    onChoosePhotoClick: () -> Unit,
    identifyMenuButton: @Composable () -> Unit,
) {
    entry<HomeRoute> {
        HomeRoot(
            layout = layout,
            onBreedClick = onBreedClick,
            onTakePhotoClick = onTakePhotoClick,
            onChoosePhotoClick = onChoosePhotoClick,
            identifyMenuButton = identifyMenuButton,
        )
    }
}
