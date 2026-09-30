package com.example.dogapp.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

class ColorContrastTest {
    private val textContrastMinimum = 4.5
    private val uiContrastMinimum = 3.0

    private fun contrast(
        first: Color,
        second: Color,
    ): Double {
        val lighter = max(first.luminance(), second.luminance())
        val darker = min(first.luminance(), second.luminance())
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun textPairs(scheme: ColorScheme) =
        mapOf(
            "onPrimary on primary" to (scheme.onPrimary to scheme.primary),
            "onPrimaryContainer on primaryContainer" to (scheme.onPrimaryContainer to scheme.primaryContainer),
            "onSecondary on secondary" to (scheme.onSecondary to scheme.secondary),
            "onSecondaryContainer on secondaryContainer" to (scheme.onSecondaryContainer to scheme.secondaryContainer),
            "onTertiary on tertiary" to (scheme.onTertiary to scheme.tertiary),
            "onTertiaryContainer on tertiaryContainer" to (scheme.onTertiaryContainer to scheme.tertiaryContainer),
            "onError on error" to (scheme.onError to scheme.error),
            "onErrorContainer on errorContainer" to (scheme.onErrorContainer to scheme.errorContainer),
            "onSurface on surface" to (scheme.onSurface to scheme.surface),
            "onSurfaceVariant on surface" to (scheme.onSurfaceVariant to scheme.surface),
            "primary on surface" to (scheme.primary to scheme.surface),
            "onSurface on surfaceContainer" to (scheme.onSurface to scheme.surfaceContainer),
        )

    private fun uiPairs(scheme: ColorScheme) =
        mapOf(
            "outline on surfaceContainerLow" to (scheme.outline to scheme.surfaceContainerLow),
            "primary on surfaceContainerLow" to (scheme.primary to scheme.surfaceContainerLow),
        )

    @Test
    fun brandSchemesMeetWcagContrastInLightAndDark() {
        val failures =
            mapOf("light" to DogAppLightColorScheme, "dark" to DogAppDarkColorScheme).flatMap { (name, scheme) ->
                textPairs(scheme).mapNotNull { (pair, colors) ->
                    contrast(colors.first, colors.second).takeIf { it < textContrastMinimum }?.let { "$name $pair: $it" }
                } +
                    uiPairs(scheme).mapNotNull { (pair, colors) ->
                        contrast(colors.first, colors.second).takeIf { it < uiContrastMinimum }?.let { "$name $pair: $it" }
                    }
            }

        assertTrue(failures.isEmpty(), "Contrast below WCAG AA:\n" + failures.joinToString("\n"))
    }
}
