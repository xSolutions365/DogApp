package com.example.dogapp.core.presentation.animation

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.example.dogapp.core.presentation.theme.DogAppMotion

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

private val NavSharedBoundsTransform =
    BoundsTransform { _, _ ->
        tween(DogAppMotion.DURATION_MEDIUM2_MILLIS, easing = DogAppMotion.EmphasizedEasing)
    }

@Composable
fun SharedElementsHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            content()
        }
    }
}

@Composable
fun Modifier.navSharedElement(key: Any): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedContentScope = LocalNavAnimatedContentScope.current
    return with(sharedTransitionScope) {
        this@navSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedContentScope,
            boundsTransform = NavSharedBoundsTransform,
        )
    }
}
