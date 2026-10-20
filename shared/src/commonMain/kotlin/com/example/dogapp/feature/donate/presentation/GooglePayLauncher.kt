package com.example.dogapp.feature.donate.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentMethod

interface GooglePayLauncher {
    fun launch(amount: DonationAmount)

    @Composable
    fun PayButton(
        onClick: () -> Unit,
        modifier: Modifier,
    )
}

@Composable
expect fun rememberGooglePayLauncher(onPaymentComplete: (PaymentMethod.GooglePay) -> Unit): GooglePayLauncher?
