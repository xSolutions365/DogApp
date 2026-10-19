package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LABiometryTypeTouchID
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberBiometricName(): String =
    remember {
        val context = LAContext()
        context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
        if (context.biometryType == LABiometryTypeTouchID) "Touch ID" else "Face ID"
    }

@Composable
actual fun rememberBiometricEnrollLauncher(): (() -> Unit)? = null
