package com.example.dogapp.core.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ObserveAsEventsTest {

    private class ControllableLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry
    }

    @Test
    fun deliversEachEventOnceWhileStartedAndHoldsEventsUntilStarted() = runComposeUiTest {
        val lifecycleOwner = ControllableLifecycleOwner().apply { registry.currentState = Lifecycle.State.CREATED }
        val events = Channel<String>(Channel.UNLIMITED)
        val received = mutableListOf<String>()
        var recompositions by mutableIntStateOf(0)

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ObserveAsEvents(events.receiveAsFlow()) { received += it }
                Text("$recompositions")
            }
        }

        events.trySend("while-created")
        waitForIdle()
        assertEquals(emptyList(), received)

        runOnIdle { lifecycleOwner.registry.currentState = Lifecycle.State.STARTED }
        waitUntil { received.isNotEmpty() }
        assertEquals(listOf("while-created"), received)

        events.trySend("first")
        events.trySend("second")
        waitUntil { received.size == 3 }

        runOnIdle { recompositions++ }
        waitForIdle()
        events.trySend("after-recomposition")
        waitUntil { received.size == 4 }

        assertEquals(listOf("while-created", "first", "second", "after-recomposition"), received)
    }
}
