package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WalletScreenTest {
    @Test
    fun materialWalletNamesTheFingerprintAndOffersEachWayForward() =
        runComposeUiTest {
            val actions = mutableListOf<WalletAction>()
            var settingsOpened = 0
            var backs = 0
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

            state = WalletUiState.Unlocked(LocalDate(2026, 10, 19))
            waitForIdle()
            onNodeWithText("Created on 19 Oct 2026").assertExists()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf(WalletAction.Unlock, WalletAction.SetUpAgain), actions)
            assertEquals(1, settingsOpened)
            assertEquals(1, backs)
        }

    @Test
    fun cupertinoWalletNamesFaceIdPointsToSettingsInWordsAndHidesWhileRecorded() =
        runComposeUiTest {
            val actions = mutableListOf<WalletAction>()
            var state: WalletUiState by mutableStateOf(WalletUiState.Locked)
            var isScreenCaptured by mutableStateOf(false)
            setContent {
                DogAppTheme {
                    WalletScreen(
                        state = state,
                        layout = WalletLayout.Cupertino,
                        biometricName = "Face ID",
                        isScreenCaptured = isScreenCaptured,
                        onAction = { actions += it },
                        onOpenSettings = null,
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

            state = WalletUiState.Unlocked(LocalDate(2026, 10, 19))
            isScreenCaptured = true
            waitForIdle()
            onNode(isHeading() and hasText("Your wallet is hidden while the screen is being recorded.")).assertExists()
            onNodeWithText("Created on 19 Oct 2026").assertDoesNotExist()

            isScreenCaptured = false
            waitForIdle()
            onNodeWithText("Created on 19 Oct 2026").assertExists()

            assertEquals(listOf(WalletAction.Unlock, WalletAction.SetUpAgain), actions)
        }
}
