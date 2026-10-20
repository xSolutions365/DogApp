package com.example.dogapp.feature.donate.presentation

import androidx.compose.runtime.Composable
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.donate.domain.PaymentMethod

@Composable
actual fun rememberGooglePayLauncher(onResult: (Outcome<PaymentMethod.GooglePay, PaymentError>) -> Unit): GooglePayLauncher? = null
