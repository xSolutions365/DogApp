package com.example.dogapp.feature.wallet.domain

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class WalletAutoLock(
    private val coordinator: WalletCoordinator,
    private val clock: Clock = Clock.System,
) {
    private var backgroundedAt: Instant? = null

    fun onAppBackground() {
        backgroundedAt = clock.now()
    }

    fun onAppForeground() {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        if (clock.now() - since >= LOCK_AFTER) coordinator.lock()
    }

    private companion object {
        val LOCK_AFTER = 30.seconds
    }
}
