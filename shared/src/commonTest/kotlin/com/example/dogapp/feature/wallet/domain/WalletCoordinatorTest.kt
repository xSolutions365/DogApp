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

    private var nextCardId = 0

    private fun coordinator() = WalletCoordinator(DefaultWalletRepository(vault, clock)) { "card-${++nextCardId}" }

    private val visa = CardValidation.Valid(brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028))

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
            assertEquals(listOf(WalletChange.SetUp), vault.writes)
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

    @Test
    fun addedCardsKeepOnlyBrandLastFourAndExpiryAndSurviveARestart() =
        runTest {
            val coordinator = coordinator()
            coordinator.unlock()

            val saved = SavedCard(id = "card-1", brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028))
            assertEquals(Outcome.Success(Wallet(firstSetUp, listOf(saved))), coordinator.addCard(visa))
            assertEquals(WalletStatus.Unlocked(Wallet(firstSetUp, listOf(saved))), coordinator.status.value)

            val afterRestart = coordinator()
            assertEquals(Outcome.Success(Wallet(firstSetUp, listOf(saved))), afterRestart.unlock())

            assertEquals(Outcome.Success(Wallet(firstSetUp)), afterRestart.removeCard("card-1"))
            assertEquals(WalletStatus.Unlocked(Wallet(firstSetUp)), afterRestart.status.value)
            assertEquals(listOf(WalletChange.SetUp, WalletChange.CardAdded, WalletChange.CardRemoved), vault.writes)
        }

    @Test
    fun cancelledOrLockedChangesLeaveTheWalletAsItWas() =
        runTest {
            val coordinator = coordinator()
            coordinator.unlock()
            val stored = checkNotNull(vault.contents)

            vault.nextFailure = WalletError.AuthenticationCancelled
            assertEquals(Outcome.Failure(WalletError.AuthenticationCancelled), coordinator.addCard(visa))
            assertEquals(WalletStatus.Unlocked(Wallet(firstSetUp)), coordinator.status.value)
            assertContentEquals(stored, vault.contents)

            coordinator.lock()
            val authentications = vault.authentications
            assertEquals(Outcome.Failure(WalletError.Locked), coordinator.addCard(visa))
            assertEquals(Outcome.Failure(WalletError.Locked), coordinator.removeCard("card-1"))
            assertEquals(authentications, vault.authentications)
            assertEquals(WalletStatus.Locked, coordinator.status.value)
        }

    @Test
    fun invalidatedKeyDuringAChangeWipesTheWallet() =
        runTest {
            val coordinator = coordinator()
            coordinator.unlock()
            vault.nextFailure = WalletError.KeyInvalidated

            assertEquals(Outcome.Failure(WalletError.KeyInvalidated), coordinator.addCard(visa))
            assertEquals(WalletStatus.KeyInvalidated, coordinator.status.value)
            assertNull(vault.contents)
        }

    @Test
    fun walletsSavedBeforeCardsExistedStillOpen() =
        runTest {
            vault.contents = """{"createdAtEpochMillis":${firstSetUp.toEpochMilliseconds()}}""".encodeToByteArray()

            assertEquals(Outcome.Success(Wallet(firstSetUp)), coordinator().unlock())
        }
}
