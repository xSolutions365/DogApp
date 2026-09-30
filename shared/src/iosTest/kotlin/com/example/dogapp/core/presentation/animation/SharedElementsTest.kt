package com.example.dogapp.core.presentation.animation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SharedElementsTest {
    private data object ListKey : NavKey

    private data object DetailKey : NavKey

    @Composable
    private fun SharedScreen(
        title: String,
        photoSize: Int,
    ) {
        Box {
            Box(Modifier.navSharedElement(key = "dog-photo").size(photoSize.dp))
            Text(title)
        }
    }

    private fun ComposeUiTest.pushAndPopBetweenScreensSharingAnElement(withHost: Boolean) {
        val backStack = mutableStateListOf<NavKey>(ListKey)
        setContent {
            val display = @Composable {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    entryProvider =
                        entryProvider {
                            entry<ListKey> { SharedScreen(title = "List", photoSize = 48) }
                            entry<DetailKey> { SharedScreen(title = "Detail", photoSize = 240) }
                        },
                )
            }
            if (withHost) SharedElementsHost { display() } else display()
        }

        onNodeWithText("List").assertExists()
        runOnIdle { backStack.add(DetailKey) }
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithTextCount("List") == 0 }
        onNodeWithText("Detail").assertExists()

        runOnIdle { backStack.removeAt(backStack.lastIndex) }
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithTextCount("Detail") == 0 }
        onNodeWithText("List").assertExists()
    }

    private fun ComposeUiTest.onAllNodesWithTextCount(text: String): Int = onAllNodes(hasText(text)).fetchSemanticsNodes().size

    @Test
    fun sharedElementAnimatesAcrossPushAndPopInsideHost() =
        runComposeUiTest {
            pushAndPopBetweenScreensSharingAnElement(withHost = true)
        }

    @Test
    fun sharedElementModifierIsInertWithoutHost() =
        runComposeUiTest {
            pushAndPopBetweenScreensSharingAnElement(withHost = false)
        }
}
