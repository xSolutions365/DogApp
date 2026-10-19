package com.example.dogapp.feature.wallet.presentation

import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.CardFieldError
import com.example.dogapp.feature.wallet.domain.SavedCard
import com.example.dogapp.feature.wallet.domain.ValidateCardUseCase
import com.example.dogapp.feature.wallet.domain.WalletChange
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletError
import com.example.dogapp.feature.wallet.domain.WalletStatus
import com.example.dogapp.testing.FakeTodayProvider
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddCardViewModelTest {
    private val vault = FakeWalletVault()
    private val coordinator = WalletCoordinator(DefaultWalletRepository(vault)) { "card-1" }
    private val viewModel by lazy { AddCardViewModel(ValidateCardUseCase(FakeTodayProvider("2026-10-19")), coordinator) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collectEvents(): List<AddCardEvent> =
        mutableListOf<AddCardEvent>().also { events ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { events += it } }
        }

    private fun enter(
        number: String,
        expiry: String,
        securityCode: String,
    ) {
        viewModel.onAction(AddCardAction.NumberChange(number))
        viewModel.onAction(AddCardAction.ExpiryChange(expiry))
        viewModel.onAction(AddCardAction.SecurityCodeChange(securityCode))
    }

    @Test
    fun typingShowsTheBrandAndInvalidFieldsAreFlaggedWithoutAPrompt() =
        runTest {
            coordinator.unlock()
            val prompts = vault.authentications

            viewModel.onAction(AddCardAction.NumberChange("37"))
            assertEquals(CardBrand.Amex, viewModel.state.value.brand)

            enter(number = "4242 4242 4242 4241", expiry = "09/26", securityCode = "12")
            viewModel.onAction(AddCardAction.Save)

            assertEquals(
                setOf(CardFieldError.InvalidNumber, CardFieldError.ExpiredCard, CardFieldError.InvalidSecurityCode),
                viewModel.state.value.errors,
            )
            assertEquals(prompts, vault.authentications)
        }

    @Test
    fun savingAValidCardStoresItClearsTheFormAndCloses() =
        runTest {
            coordinator.unlock()
            val events = collectEvents()

            enter(number = "4242 4242 4242 4242", expiry = "12/28", securityCode = "123")
            viewModel.onAction(AddCardAction.Save)

            assertEquals(listOf<AddCardEvent>(AddCardEvent.Close), events)
            assertEquals(AddCardUiState(), viewModel.state.value)
            val saved = SavedCard(id = "card-1", brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028))
            assertEquals(listOf(saved), (coordinator.status.value as WalletStatus.Unlocked).wallet.cards)
            assertEquals(WalletChange.CardAdded, vault.writes.last())
            assertTrue("4242 4242" !in checkNotNull(vault.contents).decodeToString())
        }

    @Test
    fun cancelledPromptKeepsTheFormFilledInAndALockedWalletCloses() =
        runTest {
            coordinator.unlock()
            val events = collectEvents()
            enter(number = "4242 4242 4242 4242", expiry = "12/28", securityCode = "123")

            vault.nextFailure = WalletError.AuthenticationCancelled
            viewModel.onAction(AddCardAction.Save)
            assertEquals(emptyList(), events)
            assertEquals("4242 4242 4242 4242", viewModel.state.value.number)
            assertEquals(false, viewModel.state.value.isSaving)

            coordinator.lock()
            viewModel.onAction(AddCardAction.Save)
            assertEquals(listOf<AddCardEvent>(AddCardEvent.Close), events)
        }
}
