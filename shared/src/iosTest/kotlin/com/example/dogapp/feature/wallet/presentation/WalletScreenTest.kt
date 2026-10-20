package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.SavedCard
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WalletScreenTest {
    private val visa = SavedCard(id = "card-1", brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028))

    @Test
    fun materialWalletNamesTheFingerprintAndOffersEachWayForward() =
        runComposeUiTest {
            val actions = mutableListOf<WalletAction>()
            var settingsOpened = 0
            var backs = 0
            var addCardClicks = 0
            val openedCards = mutableListOf<String>()
            var state: WalletUiState by mutableStateOf(WalletUiState.Locked)
            setContent {
                DogAppTheme {
                    WalletScreen(
                        state = state,
                        layout = WalletLayout.Material,
                        biometricName = "fingerprint",
                        isScreenCaptured = false,
                        onAction = { actions += it },
                        onOpenSettings = { settingsOpened++ },
                        onAddCardClick = { addCardClicks++ },
                        onCardClick = { openedCards += it },
                        onBack = { backs++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Wallet locked")).assertExists()
            onNodeWithText("Use your fingerprint to unlock").assertExists()
            onNodeWithText("Unlock").performClick()

            state = WalletUiState.KeyInvalidated
            waitForIdle()
            onNode(isHeading() and hasText("Wallet reset")).assertExists()
            onNodeWithText("A new fingerprint was added, so your wallet was cleared to keep it safe.").assertExists()
            onNodeWithText("Set up again").performClick()

            state = WalletUiState.BiometricUnavailable
            waitForIdle()
            onNode(isHeading() and hasText("Add a fingerprint to use your wallet")).assertExists()
            onNodeWithText("Open settings").performClick()

            state = WalletUiState.Unlocked(listOf(visa))
            waitForIdle()
            onNodeWithText("Visa •••• 4242").assertExists()
            onNodeWithText("Expires 12/28").assertExists()
            onNodeWithText("Visa •••• 4242").performClick()
            onNodeWithContentDescription("Add card").performClick()

            state = WalletUiState.Unlocked(emptyList())
            waitForIdle()
            onNodeWithText("No cards yet").assertExists()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf(WalletAction.Unlock, WalletAction.SetUpAgain), actions)
            assertEquals(listOf("card-1"), openedCards)
            assertEquals(1, settingsOpened)
            assertEquals(1, backs)
            assertEquals(1, addCardClicks)
        }

    @Test
    fun cupertinoWalletNamesFaceIdPointsToSettingsInWordsAndHidesWhileRecorded() =
        runComposeUiTest {
            val actions = mutableListOf<WalletAction>()
            var state: WalletUiState by mutableStateOf(WalletUiState.Locked)
            var isScreenCaptured by mutableStateOf(false)
            var addCardClicks = 0
            val openedCards = mutableListOf<String>()
            setContent {
                DogAppTheme {
                    WalletScreen(
                        state = state,
                        layout = WalletLayout.Cupertino,
                        biometricName = "Face ID",
                        isScreenCaptured = isScreenCaptured,
                        onAction = { actions += it },
                        onOpenSettings = null,
                        onAddCardClick = { addCardClicks++ },
                        onCardClick = { openedCards += it },
                        onBack = {},
                    )
                }
            }

            onNode(isHeading() and hasText("Wallet Locked")).assertExists()
            onNodeWithText("Use Face ID to view your wallet.").assertExists()
            onNodeWithText("Unlock with Face ID").performClick()

            state = WalletUiState.KeyInvalidated
            waitForIdle()
            onNode(isHeading() and hasText("Wallet Reset")).assertExists()
            onNodeWithText("Face ID was changed, so your wallet was cleared to keep it safe.").assertExists()
            onNodeWithText("Set Up Again").performClick()

            state = WalletUiState.BiometricUnavailable
            waitForIdle()
            onNode(isHeading() and hasText("Turn on Face ID in Settings to use your wallet.")).assertExists()
            onNodeWithText("Open settings").assertDoesNotExist()

            state = WalletUiState.Unlocked(listOf(visa))
            isScreenCaptured = true
            waitForIdle()
            onNode(isHeading() and hasText("Your wallet is hidden while the screen is being recorded.")).assertExists()
            onNodeWithText("Visa •••• 4242").assertDoesNotExist()
            onNodeWithText("Add Card").assertDoesNotExist()

            isScreenCaptured = false
            waitForIdle()
            onNodeWithText("Visa •••• 4242").assertExists()
            onNodeWithText("Add Card").performClick()
            onNodeWithContentDescription("Add card").assertDoesNotExist()
            onNodeWithText("Visa •••• 4242").performClick()
            onNodeWithText("Visa •••• 4242").performTouchInput { swipeLeft() }
            waitForIdle()
            onNodeWithText("Visa •••• 4242").assertLeftPositionInRootIsEqualTo(16.dp)
            val remove =
                onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))
                    .fetchSemanticsNode()
                    .config[SemanticsActions.CustomActions]
                    .single { it.label == "Remove" }
            runOnIdle { remove.action() }

            assertEquals(
                listOf(WalletAction.Unlock, WalletAction.SetUpAgain, WalletAction.RemoveCard("card-1"), WalletAction.RemoveCard("card-1")),
                actions,
            )
            assertEquals(1, addCardClicks)
            assertEquals(emptyList(), openedCards)
        }
}
