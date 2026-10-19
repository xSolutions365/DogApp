package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun SecureScreenEffect()

@Composable
expect fun rememberIsScreenCaptured(): Boolean
