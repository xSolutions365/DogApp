package com.example.dogapp.core.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

object DogAppMotion {
    const val DURATION_SHORT2_MILLIS = 100
    const val DURATION_MEDIUM2_MILLIS = 300
    const val DURATION_MEDIUM3_MILLIS = 350

    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerateEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerateEasing: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val CupertinoEasing: Easing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)
}
