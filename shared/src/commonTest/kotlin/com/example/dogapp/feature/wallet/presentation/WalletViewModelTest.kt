package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
    private val berlin = TimeZone.of("Europe/Berlin")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.openWallet(): WalletViewModel =
        WalletViewModel(coordinator) { berlin }.also { viewModel ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        }

    @Test
    fun openingTheWalletAsksForTheBiometricOnceAndShowsTheLocalCreationDay() =
        runTest {
            val viewModel = openWallet()

            assertEquals(WalletUiState.Unlocked(LocalDate(2026, 10, 20)), viewModel.state.value)
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
            assertEquals(WalletUiState.Unlocked(LocalDate(2026, 10, 20)), viewModel.state.value)
        }

    @Test
    fun missingBiometricIsExplainedAndRetriedWhenTheScreenResumes() =
        runTest {
            vault.nextFailure = WalletError.BiometricUnavailable
            val viewModel = openWallet()

            assertEquals(WalletUiState.BiometricUnavailable, viewModel.state.value)

            viewModel.onAction(WalletAction.ScreenResume)
            assertEquals(WalletUiState.Unlocked(LocalDate(2026, 10, 20)), viewModel.state.value)
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
            assertEquals(WalletUiState.Unlocked(LocalDate(2026, 10, 20)), viewModel.state.value)
        }
}
