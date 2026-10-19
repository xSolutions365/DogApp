package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.CardValidation
import com.example.dogapp.feature.wallet.domain.SavedCard
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

@OptIn(ExperimentalCoroutinesApi::class)
class CardDetailViewModelTest {
    private val vault = FakeWalletVault()
    private val coordinator = WalletCoordinator(DefaultWalletRepository(vault)) { "card-1" }
    private val visa = SavedCard(id = "card-1", brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun walletWithVisa() {
        coordinator.unlock()
        coordinator.addCard(CardValidation.Valid(brand = visa.brand, last4 = visa.last4, expiry = visa.expiry))
    }

    private fun TestScope.open(): Pair<CardDetailViewModel, List<CardDetailEvent>> {
        val viewModel = CardDetailViewModel("card-1", coordinator)
        val events = mutableListOf<CardDetailEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { events += it } }
        return viewModel to events
    }

    @Test
    fun removingTheCardAsksForTheBiometricAndClosesOnceItIsGone() =
        runTest {
            walletWithVisa()
            val (viewModel, events) = open()
            assertEquals(CardDetailUiState(card = visa), viewModel.state.value)

            viewModel.onAction(CardDetailAction.Remove)

            assertEquals(listOf<CardDetailEvent>(CardDetailEvent.Close), events)
            assertEquals(WalletChange.CardRemoved, vault.writes.last())
        }

    @Test
    fun cancelledPromptKeepsTheCardAndAnAutoLockCloses() =
        runTest {
            walletWithVisa()
            val (viewModel, events) = open()

            vault.nextFailure = WalletError.AuthenticationCancelled
            viewModel.onAction(CardDetailAction.Remove)
            assertEquals(CardDetailUiState(card = visa), viewModel.state.value)
            assertEquals(emptyList(), events)

            coordinator.lock()
            assertEquals(listOf<CardDetailEvent>(CardDetailEvent.Close), events)
        }
}
