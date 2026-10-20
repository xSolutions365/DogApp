package com.example.dogapp.feature.profile.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.domain.security.IntegrityCheck
import com.example.dogapp.core.domain.security.IntegrityCheckKind
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DeviceChecksScreenTest {
    @Test
    fun everyCheckShowsItsResultAndTheScreenSaysTheChecksAreEasyToBypass() =
        runComposeUiTest {
            var backs = 0
            setContent {
                DogAppTheme {
                    DeviceChecksScreen(
                        state =
                            DeviceChecksUiState(
                                IntegrityCheckKind.entries.map { kind ->
                                    IntegrityCheck(
                                        kind,
                                        isFlagged =
                                            kind == IntegrityCheckKind.Emulator || kind == IntegrityCheckKind.Debugger,
                                    )
                                },
                            ),
                        onBack = { backs++ },
                    )
                }
            }

            onNode(isHeading() and hasText("Device checks")).assertExists()
            onNodeWithText(
                "These checks run on the device and are easy to bypass. They never block anything. " +
                    "Real apps confirm a device's integrity with a server (Play Integrity on Android, App Attest on iOS).",
            ).assertExists()
            listOf(
                "Root or Magisk" to "Not found",
                "Test-keys build" to "Not found",
                "App signature" to "Matches",
                "Emulator" to "Warning",
                "Jailbreak files" to "Not found",
                "Write outside the sandbox" to "Blocked",
                "Simulator" to "Not detected",
                "Debugger" to "Warning",
            ).forEach { (label, result) ->
                onNode(hasText(label) and hasText(result)).assertExists()
            }
            onNodeWithContentDescription("Back").performClick()
            assertEquals(1, backs)
        }
}
