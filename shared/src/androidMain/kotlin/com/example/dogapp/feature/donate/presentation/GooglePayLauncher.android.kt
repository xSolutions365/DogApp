package com.example.dogapp.feature.donate.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.donate.domain.PaymentMethod
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

@Composable
actual fun rememberGooglePayLauncher(onResult: (Outcome<PaymentMethod.GooglePay, PaymentError>) -> Unit): GooglePayLauncher? {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val client =
        remember(context) {
            Wallet.getPaymentsClient(
                context,
                Wallet.WalletOptions
                    .Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build(),
            )
        }
    var isReady by remember(client) { mutableStateOf(false) }
    DisposableEffect(client) {
        var active = true
        client.isReadyToPay(IsReadyToPayRequest.fromJson(GooglePayRequests.isReadyToPay())).addOnCompleteListener { task ->
            if (active) isReady = task.isSuccessful && task.result == true
        }
        onDispose { active = false }
    }
    val launcher =
        rememberLauncherForActivityResult(TaskResultContracts.GetPaymentDataResult()) { result ->
            val outcome =
                when (result.status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        result.result
                            ?.toJson()
                            ?.let(GooglePayRequests::googlePayMethod)
                            ?.let { Outcome.Success(it) }
                            ?: Outcome.Failure(PaymentError.GooglePayFailed)
                    }

                    CommonStatusCodes.CANCELED -> {
                        Outcome.Failure(PaymentError.Cancelled)
                    }

                    else -> {
                        Logger.w { "Google Pay failed with status ${result.status.statusCode}" }
                        Outcome.Failure(PaymentError.GooglePayFailed)
                    }
                }
            currentOnResult(outcome)
        }
    return remember(client, launcher, isReady) {
        if (isReady) AndroidGooglePayLauncher(client) { request -> launcher.launch(request) } else null
    }
}

private class AndroidGooglePayLauncher(
    private val client: PaymentsClient,
    private val launchTask: (Task<PaymentData>) -> Unit,
) : GooglePayLauncher {
    override fun launch(amount: DonationAmount) {
        launchTask(client.loadPaymentData(PaymentDataRequest.fromJson(GooglePayRequests.paymentData(amount))))
    }

    @Composable
    override fun PayButton(
        onClick: () -> Unit,
        modifier: Modifier,
    ) {
        PayButton(
            onClick = onClick,
            allowedPaymentMethods = GooglePayRequests.allowedPaymentMethods,
            theme = if (isSystemInDarkTheme()) ButtonTheme.Light else ButtonTheme.Dark,
            type = ButtonType.Donate,
            modifier = modifier,
        )
    }
}
