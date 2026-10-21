package com.example.dogapp.feature.identify.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CameraScreenTest {
    @Test
    fun theCameraShowsTheViewfinderWhenAllowedOffersTheShutterOnceStartedAndOtherwiseExplainsWithAWayForward() =
        runComposeUiTest {
            val clicks = mutableListOf<String>()
            var access by mutableStateOf<CameraAccess?>(null)
            var isStarting by mutableStateOf(true)
            var isCapturing by mutableStateOf(false)
            setContent {
                DogAppTheme {
                    CameraScreen(
                        access = access,
                        isStarting = isStarting,
                        isCapturing = isCapturing,
                        onShutterClick = { clicks += "shutter" },
                        onAllowAccess = { clicks += "allow" },
                        onOpenSettings = { clicks += "settings" },
                        onChoosePhoto = { clicks += "choose" },
                        onClose = { clicks += "close" },
                    ) {
                        Box(Modifier.testTag("viewfinder"))
                    }
                }
            }

            onNodeWithTag("viewfinder").assertDoesNotExist()
            onNodeWithText("Camera access is off").assertDoesNotExist()
            onNodeWithContentDescription("Close camera").performClick()

            access = CameraAccess.Granted
            waitForIdle()
            onNodeWithTag("viewfinder").assertExists()
            onNodeWithContentDescription("Take photo").assertIsNotEnabled().performClick()
            isStarting = false
            waitForIdle()
            onNodeWithContentDescription("Take photo").assertIsEnabled().performClick()
            isCapturing = true
            waitForIdle()
            onNodeWithContentDescription("Take photo").assertIsNotEnabled()

            access = CameraAccess.Denied
            waitForIdle()
            onNodeWithTag("viewfinder").assertDoesNotExist()
            onNode(isHeading() and hasText("Camera access is off")).assertExists()
            onNodeWithText("DogApp needs the camera to take a photo of a dog. You can still choose a photo.").assertExists()
            onNodeWithText("Open settings").assertDoesNotExist()
            onNodeWithText("Allow camera").performClick()
            onNodeWithText("Choose a photo").performClick()

            access = CameraAccess.PermanentlyDenied
            waitForIdle()
            onNodeWithText("Allow camera").assertDoesNotExist()
            onNodeWithText("Open settings").performClick()

            access = CameraAccess.Unavailable
            waitForIdle()
            onNodeWithText("Camera access is off").assertDoesNotExist()
            onNodeWithText("This device has no camera. You can still choose a photo.").assertExists()
            onNodeWithText("Open settings").assertDoesNotExist()
            onNodeWithText("Choose a photo").performClick()

            assertEquals(listOf("close", "shutter", "allow", "choose", "settings", "choose"), clicks)
        }
}
