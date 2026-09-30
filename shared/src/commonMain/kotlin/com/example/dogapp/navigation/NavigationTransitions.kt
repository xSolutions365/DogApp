package com.example.dogapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

typealias NavigationTransition = AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

private const val MATERIAL_DURATION_MILLIS = 300
private const val MATERIAL_FADE_OUT_MILLIS = 90
private const val MATERIAL_FADE_IN_DELAY_MILLIS = 90
private const val MATERIAL_SLIDE_FRACTION = 10
private const val CUPERTINO_DURATION_MILLIS = 350
private const val CUPERTINO_PARALLAX_FRACTION = 3
private val CupertinoEasing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)

private fun materialSharedAxisX(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    val enter = fadeIn(
        tween(MATERIAL_DURATION_MILLIS - MATERIAL_FADE_IN_DELAY_MILLIS, MATERIAL_FADE_IN_DELAY_MILLIS, LinearOutSlowInEasing)
    ) + slideInHorizontally(tween(MATERIAL_DURATION_MILLIS)) { fullWidth -> direction * fullWidth / MATERIAL_SLIDE_FRACTION }
    val exit = fadeOut(
        tween(MATERIAL_FADE_OUT_MILLIS, easing = FastOutLinearInEasing)
    ) + slideOutHorizontally(tween(MATERIAL_DURATION_MILLIS)) { fullWidth -> -direction * fullWidth / MATERIAL_SLIDE_FRACTION }
    return enter togetherWith exit
}

private fun cupertinoPush(): ContentTransform =
    slideInHorizontally(tween(CUPERTINO_DURATION_MILLIS, easing = CupertinoEasing)) { fullWidth -> fullWidth } togetherWith
        slideOutHorizontally(tween(CUPERTINO_DURATION_MILLIS, easing = CupertinoEasing)) { fullWidth ->
            -fullWidth / CUPERTINO_PARALLAX_FRACTION
        }

private fun cupertinoPop(): ContentTransform =
    (
        slideInHorizontally(tween(CUPERTINO_DURATION_MILLIS, easing = CupertinoEasing)) { fullWidth ->
            -fullWidth / CUPERTINO_PARALLAX_FRACTION
        } togetherWith slideOutHorizontally(tween(CUPERTINO_DURATION_MILLIS, easing = CupertinoEasing)) { fullWidth -> fullWidth }
    ).apply { targetContentZIndex = -1f }

fun NavigationStyle.pushTransition(): NavigationTransition = when (this) {
    NavigationStyle.Material -> { { materialSharedAxisX(forward = true) } }
    NavigationStyle.Cupertino -> { { cupertinoPush() } }
}

fun NavigationStyle.popTransition(): NavigationTransition = when (this) {
    NavigationStyle.Material -> { { materialSharedAxisX(forward = false) } }
    NavigationStyle.Cupertino -> { { cupertinoPop() } }
}

fun tabSelectionTransition(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None
