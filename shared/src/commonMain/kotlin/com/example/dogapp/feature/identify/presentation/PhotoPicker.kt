package com.example.dogapp.feature.identify.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPicker(onPhotoPick: (ByteArray) -> Unit): () -> Unit
