package com.example.dogapp.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class DogAppThemeTest {

    @Test
    fun themeAppliesBrandColorSchemesAndTypePairing() {
        listOf(false to DogAppLightColorScheme, true to DogAppDarkColorScheme).forEach { (darkTheme, expectedScheme) ->
            runComposeUiTest {
                lateinit var colorScheme: ColorScheme
                lateinit var typography: Typography
                setContent {
                    DogAppTheme(darkTheme = darkTheme) {
                        colorScheme = MaterialTheme.colorScheme
                        typography = MaterialTheme.typography
                        Text("Border Collie", style = MaterialTheme.typography.headlineLarge)
                        Text("12–20 kg", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                waitForIdle()

                assertEquals(expectedScheme.primary, colorScheme.primary)
                assertEquals(expectedScheme.tertiaryContainer, colorScheme.tertiaryContainer)
                assertEquals(expectedScheme.surface, colorScheme.surface)

                val displayFamily = assertNotNull(typography.headlineLarge.fontFamily)
                val bodyFamily = assertNotNull(typography.bodyLarge.fontFamily)
                assertNotEquals(displayFamily, bodyFamily)
                listOf(typography.displayLarge, typography.headlineMedium, typography.titleLarge).forEach {
                    assertEquals(displayFamily, it.fontFamily)
                }
                listOf(typography.titleMedium, typography.bodyMedium, typography.labelSmall).forEach {
                    assertEquals(bodyFamily, it.fontFamily)
                }
                assertEquals(FontWeight.ExtraBold, typography.headlineLarge.fontWeight)
                assertEquals(FontWeight.Normal, typography.bodyLarge.fontWeight)
            }
        }
    }
}
