package com.example.dogapp.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun IdentifyMenuButton(
    onTakePhotoClick: () -> Unit,
    onChoosePhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
)
