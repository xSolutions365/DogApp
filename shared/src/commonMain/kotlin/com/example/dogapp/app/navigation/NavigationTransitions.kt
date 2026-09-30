package com.example.dogapp.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.example.dogapp.core.presentation.theme.DogAppMotion

typealias NavigationTransition = AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

private const val MATERIAL_SLIDE_FRACTION = 10
private const val CUPERTINO_PARALLAX_FRACTION = 3

private fun materialSharedAxisX(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    val enter =
        fadeIn(
            tween(
                durationMillis = DogAppMotion.DURATION_MEDIUM2_MILLIS - DogAppMotion.DURATION_SHORT2_MILLIS,
                delayMillis = DogAppMotion.DURATION_SHORT2_MILLIS,
                easing = DogAppMotion.EmphasizedDecelerateEasing,
            ),
        ) +
            slideInHorizontally(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS, easing = DogAppMotion.EmphasizedEasing)) { fullWidth ->
                direction * fullWidth / MATERIAL_SLIDE_FRACTION
            }
    val exit =
        fadeOut(
            tween(DogAppMotion.DURATION_SHORT2_MILLIS, easing = DogAppMotion.EmphasizedAccelerateEasing),
        ) +
            slideOutHorizontally(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS, easing = DogAppMotion.EmphasizedEasing)) { fullWidth ->
                -direction * fullWidth / MATERIAL_SLIDE_FRACTION
            }
    return enter togetherWith exit
}

private fun cupertinoPush(): ContentTransform =
    slideInHorizontally(
        tween(DogAppMotion.DURATION_MEDIUM3_MILLIS, easing = DogAppMotion.CupertinoEasing),
    ) { fullWidth -> fullWidth } togetherWith
        slideOutHorizontally(tween(DogAppMotion.DURATION_MEDIUM3_MILLIS, easing = DogAppMotion.CupertinoEasing)) { fullWidth ->
            -fullWidth / CUPERTINO_PARALLAX_FRACTION
        }

private fun cupertinoPop(): ContentTransform =
    (
        slideInHorizontally(tween(DogAppMotion.DURATION_MEDIUM3_MILLIS, easing = DogAppMotion.CupertinoEasing)) { fullWidth ->
            -fullWidth / CUPERTINO_PARALLAX_FRACTION
        } togetherWith
            slideOutHorizontally(
                tween(DogAppMotion.DURATION_MEDIUM3_MILLIS, easing = DogAppMotion.CupertinoEasing),
            ) { fullWidth -> fullWidth }
    ).apply { targetContentZIndex = -1f }

fun NavigationStyle.pushTransition(): NavigationTransition =
    when (this) {
        NavigationStyle.Material -> {
            { materialSharedAxisX(forward = true) }
        }

        NavigationStyle.Cupertino -> {
            { cupertinoPush() }
        }
    }

fun NavigationStyle.popTransition(): NavigationTransition =
    when (this) {
        NavigationStyle.Material -> {
            { materialSharedAxisX(forward = false) }
        }

        NavigationStyle.Cupertino -> {
            { cupertinoPop() }
        }
    }

fun tabSelectionTransition(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None
