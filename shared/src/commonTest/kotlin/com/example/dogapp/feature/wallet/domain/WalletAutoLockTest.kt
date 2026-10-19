package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.testing.FakeWalletVault
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class WalletAutoLockTest {
    private class MutableClock(
        var now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }

    private val clock = MutableClock(Instant.parse("2026-10-19T09:00:00Z"))
    private val coordinator = WalletCoordinator(DefaultWalletRepository(FakeWalletVault(), clock))
    private val autoLock = WalletAutoLock(coordinator, clock)

    private fun spendInBackground(duration: Duration) {
        autoLock.onAppBackground()
        clock.now += duration
        autoLock.onAppForeground()
    }

    @Test
    fun shortTripsAwayKeepTheWalletUnlockedAndThirtySecondsLocksIt() =
        runTest {
            coordinator.unlock()

            spendInBackground(29.seconds)
            assertIs<WalletStatus.Unlocked>(coordinator.status.value)

            spendInBackground(30.seconds)
            assertEquals(WalletStatus.Locked, coordinator.status.value)
        }

    @Test
    fun onlyTheLatestTripAwayCounts() =
        runTest {
            coordinator.unlock()
            spendInBackground(10.seconds)
            clock.now += 5.seconds

            autoLock.onAppForeground()
            assertIs<WalletStatus.Unlocked>(coordinator.status.value)

            spendInBackground(20.seconds)
            assertIs<WalletStatus.Unlocked>(coordinator.status.value)
        }
}
