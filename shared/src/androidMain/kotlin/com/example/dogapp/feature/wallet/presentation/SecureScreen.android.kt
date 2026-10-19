package com.example.dogapp.feature.wallet.presentation

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import java.util.WeakHashMap

private val secureScreens = WeakHashMap<Activity, Int>()

@Composable
actual fun SecureScreenEffect() {
    val activity = checkNotNull(LocalActivity.current) { "SecureScreenEffect needs an Activity" }
    DisposableEffect(activity) {
        val count = secureScreens.getOrDefault(activity, 0)
        if (count == 0) activity.setSecure(true)
        secureScreens[activity] = count + 1
        onDispose {
            val remaining = secureScreens.getOrDefault(activity, 1) - 1
            if (remaining == 0) {
                secureScreens.remove(activity)
                activity.setSecure(false)
            } else {
                secureScreens[activity] = remaining
            }
        }
    }
}

private fun Activity.setSecure(secure: Boolean) {
    if (secure) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setRecentsScreenshotEnabled(!secure)
}

@Composable
actual fun rememberIsScreenCaptured(): Boolean = false
