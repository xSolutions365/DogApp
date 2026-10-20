package com.example.dogapp.feature.donate.presentation

import androidx.compose.runtime.Composable
import com.example.dogapp.feature.donate.domain.PaymentMethod

@Composable
actual fun rememberGooglePayLauncher(onPaymentComplete: (PaymentMethod.GooglePay) -> Unit): GooglePayLauncher? = null
