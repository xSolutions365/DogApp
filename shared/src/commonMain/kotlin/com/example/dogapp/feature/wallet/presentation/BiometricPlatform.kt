package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun rememberBiometricName(): String

@Composable
expect fun rememberBiometricEnrollLauncher(): (() -> Unit)?
