package com.example.dogapp.feature.wallet.presentation

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_biometric_fingerprint
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberBiometricName(): String = stringResource(Res.string.wallet_biometric_fingerprint)

@Composable
actual fun rememberBiometricEnrollLauncher(): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context) {
        {
            val intent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG)
                } else {
                    Intent(Settings.ACTION_SECURITY_SETTINGS)
                }
            context.startActivity(intent)
        }
    }
}
