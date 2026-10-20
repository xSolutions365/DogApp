package com.example.dogapp.feature.identify.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoCapture(
    onPhotoCapture: (ByteArray) -> Unit,
    onChoosePhoto: () -> Unit,
): () -> Unit
