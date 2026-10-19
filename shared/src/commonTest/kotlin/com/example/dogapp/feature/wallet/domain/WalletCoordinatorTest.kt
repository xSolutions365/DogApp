package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.testing.FakeWalletVault
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class WalletCoordinatorTest {
    private class MutableClock(
        var now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }

    private val firstSetUp = Instant.parse("2026-10-19T09:15:00Z")
    private val clock = MutableClock(firstSetUp)
    private val vault = FakeWalletVault()

    private fun coordinator() = WalletCoordinator(DefaultWalletRepository(vault, clock))

    @Test
    fun firstUnlockCreatesTheWalletAndLaterUnlocksReadItBack() =
        runTest {
            val coordinator = coordinator()
            assertEquals(WalletStatus.Locked, coordinator.status.value)

            assertEquals(Outcome.Success(Wallet(firstSetUp)), coordinator.unlock())
            assertEquals(WalletStatus.Unlocked(Wallet(firstSetUp)), coordinator.status.value)
            assertNotNull(vault.contents)

            coordinator.lock()
            assertEquals(WalletStatus.Locked, coordinator.status.value)

            clock.now = Instant.parse("2026-10-20T08:00:00Z")
            val afterRestart = coordinator()
            assertEquals(Outcome.Success(Wallet(firstSetUp)), afterRestart.unlock())
            assertEquals(WalletStatus.Unlocked(Wallet(firstSetUp)), afterRestart.status.value)
            assertEquals(2, vault.authentications)
        }

    @Test
    fun unsuccessfulAuthenticationKeepsTheWalletLockedAndStored() =
        runTest {
            val coordinator = coordinator()
            coordinator.unlock()
            coordinator.lock()
            val stored = checkNotNull(vault.contents)

            listOf(
                WalletError.AuthenticationCancelled,
                WalletError.AuthenticationFailed,
                WalletError.BiometricUnavailable,
            ).forEach { error ->
                vault.nextFailure = error

                assertEquals(Outcome.Failure(error), coordinator.unlock())
                assertEquals(WalletStatus.Locked, coordinator.status.value)
                assertContentEquals(stored, vault.contents)
            }
        }

    @Test
    fun invalidatedKeyWipesTheWalletUntilItIsSetUpAgain() =
        runTest {
            val coordinator = coordinator()
            coordinator.unlock()
            coordinator.lock()
            vault.nextFailure = WalletError.KeyInvalidated

            assertEquals(Outcome.Failure(WalletError.KeyInvalidated), coordinator.unlock())
            assertEquals(WalletStatus.KeyInvalidated, coordinator.status.value)
            assertNull(vault.contents)

            coordinator.lock()
            assertEquals(WalletStatus.KeyInvalidated, coordinator.status.value)

            val setUpAgain = Instant.parse("2026-10-19T10:00:00Z")
            clock.now = setUpAgain
            assertEquals(Outcome.Success(Wallet(setUpAgain)), coordinator.unlock())
            assertEquals(WalletStatus.Unlocked(Wallet(setUpAgain)), coordinator.status.value)
        }
}
