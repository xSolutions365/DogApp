package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.CardValidation
import com.example.dogapp.feature.wallet.domain.WalletChange
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletError
import com.example.dogapp.testing.FakeWalletVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelTest {
    private val vault = FakeWalletVault()
    private val clock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2026-10-19T23:30:00Z")
        }
    private val coordinator = WalletCoordinator(DefaultWalletRepository(vault, clock))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.openWallet(): WalletViewModel =
        WalletViewModel(coordinator).also { viewModel ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        }

    @Test
    fun openingTheWalletAsksForTheBiometricOnceAndShowsItsCards() =
        runTest {
            val viewModel = openWallet()

            assertEquals(WalletUiState.Unlocked(emptyList()), viewModel.state.value)
            assertEquals(1, vault.authentications)

            openWallet()
            assertEquals(1, vault.authentications)
        }

    @Test
    fun cancelledPromptLeavesTheWalletLockedUntilUnlockIsTapped() =
        runTest {
            vault.nextFailure = WalletError.AuthenticationCancelled
            val viewModel = openWallet()

            assertEquals(WalletUiState.Locked, viewModel.state.value)
            viewModel.onAction(WalletAction.ScreenResume)
            assertEquals(1, vault.authentications)

            viewModel.onAction(WalletAction.Unlock)
            assertEquals(WalletUiState.Unlocked(emptyList()), viewModel.state.value)
        }

    @Test
    fun missingBiometricIsExplainedAndRetriedWhenTheScreenResumes() =
        runTest {
            vault.nextFailure = WalletError.BiometricUnavailable
            val viewModel = openWallet()

            assertEquals(WalletUiState.BiometricUnavailable, viewModel.state.value)

            viewModel.onAction(WalletAction.ScreenResume)
            assertEquals(WalletUiState.Unlocked(emptyList()), viewModel.state.value)
            assertEquals(2, vault.authentications)
        }

    @Test
    fun invalidatedKeyShowsTheResetUntilTheWalletIsSetUpAgain() =
        runTest {
            coordinator.unlock()
            coordinator.lock()
            vault.nextFailure = WalletError.KeyInvalidated
            val viewModel = openWallet()

            assertEquals(WalletUiState.KeyInvalidated, viewModel.state.value)

            viewModel.onAction(WalletAction.SetUpAgain)
            assertEquals(WalletUiState.Unlocked(emptyList()), viewModel.state.value)
        }

    @Test
    fun removingACardFromTheListAsksForTheBiometricAndUpdatesTheList() =
        runTest {
            val viewModel = openWallet()
            coordinator.addCard(CardValidation.Valid(brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028)))
            assertEquals(1, (viewModel.state.value as WalletUiState.Unlocked).cards.size)

            viewModel.onAction(WalletAction.RemoveCard((viewModel.state.value as WalletUiState.Unlocked).cards.single().id))

            assertEquals(WalletUiState.Unlocked(emptyList()), viewModel.state.value)
            assertEquals(WalletChange.CardRemoved, vault.writes.last())
        }
}
