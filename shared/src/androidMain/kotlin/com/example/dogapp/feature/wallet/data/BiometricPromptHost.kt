package com.example.dogapp.feature.wallet.data

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.domain.WalletError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

class BiometricPromptHost(
    context: Context,
) {
    private var activity: WeakReference<FragmentActivity>? = null

    init {
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) = Unit

                override fun onActivityStarted(activity: Activity) {
                    if (activity is FragmentActivity) this@BiometricPromptHost.activity = WeakReference(activity)
                }

                override fun onActivityResumed(activity: Activity) = Unit

                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) = Unit

                override fun onActivityDestroyed(activity: Activity) {
                    if (this@BiometricPromptHost.activity?.get() === activity) this@BiometricPromptHost.activity = null
                }
            },
        )
    }

    suspend fun authenticate(
        title: String,
        negativeButtonText: String,
        cryptoObject: BiometricPrompt.CryptoObject,
    ): Outcome<BiometricPrompt.CryptoObject, WalletError> =
        withContext(Dispatchers.Main) {
            val activity = checkNotNull(activity?.get()) { "No FragmentActivity is started to host the biometric prompt" }
            suspendCancellableCoroutine { continuation ->
                val prompt =
                    BiometricPrompt(
                        activity,
                        ContextCompat.getMainExecutor(activity),
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                continuation.resume(Outcome.Success(checkNotNull(result.cryptoObject)))
                            }

                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence,
                            ) {
                                continuation.resume(Outcome.Failure(errorCode.toWalletError()))
                            }
                        },
                    )
                continuation.invokeOnCancellation { activity.runOnUiThread { prompt.cancelAuthentication() } }
                prompt.authenticate(
                    BiometricPrompt.PromptInfo
                        .Builder()
                        .setTitle(title)
                        .setNegativeButtonText(negativeButtonText)
                        .setAllowedAuthenticators(BIOMETRIC_STRONG)
                        .build(),
                    cryptoObject,
                )
            }
        }

    private fun Int.toWalletError(): WalletError =
        when (this) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED,
            -> WalletError.AuthenticationCancelled

            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_HW_UNAVAILABLE,
            BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED,
            -> WalletError.BiometricUnavailable

            else -> WalletError.AuthenticationFailed
        }
}
