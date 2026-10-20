package com.example.dogapp.feature.identify.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.example.dogapp.core.presentation.animation.IDENTIFY_SHARED_KEY
import com.example.dogapp.core.presentation.animation.navSharedBounds
import com.example.dogapp.core.presentation.theme.DogAppMotion

private val containerTransform =
    fadeIn(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS)) togetherWith fadeOut(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS))

fun EntryProviderScope<NavKey>.identifyEntries(
    onBreedClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    entry<IdentifyRoute>(
        metadata =
            NavDisplay.transitionSpec { containerTransform } +
                NavDisplay.popTransitionSpec { containerTransform } +
                NavDisplay.predictivePopTransitionSpec { containerTransform },
    ) { route ->
        IdentifyRoot(
            presentation = IdentifyPresentation.Screen,
            source = route.source,
            onBreedClick = onBreedClick,
            onDismiss = onBack,
            modifier = Modifier.navSharedBounds(IDENTIFY_SHARED_KEY, FloatingActionButtonDefaults.shape),
        )
    }
}
