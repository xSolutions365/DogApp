package com.example.dogapp.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.bricolage_grotesque_bold
import dogapp.shared.generated.resources.bricolage_grotesque_extrabold
import dogapp.shared.generated.resources.figtree_bold
import dogapp.shared.generated.resources.figtree_medium
import dogapp.shared.generated.resources.figtree_regular
import dogapp.shared.generated.resources.figtree_semibold
import org.jetbrains.compose.resources.Font

private val DisplayLetterSpacing = (-0.02).em

@Composable
fun bricolageGrotesqueFamily(): FontFamily = FontFamily(
    Font(Res.font.bricolage_grotesque_bold, FontWeight.Bold),
    Font(Res.font.bricolage_grotesque_extrabold, FontWeight.ExtraBold),
)

@Composable
fun figtreeFamily(): FontFamily = FontFamily(
    Font(Res.font.figtree_regular, FontWeight.Normal),
    Font(Res.font.figtree_medium, FontWeight.Medium),
    Font(Res.font.figtree_semibold, FontWeight.SemiBold),
    Font(Res.font.figtree_bold, FontWeight.Bold),
)

@Composable
fun dogAppTypography(): Typography {
    val display = bricolageGrotesqueFamily()
    val body = figtreeFamily()
    val base = Typography()
    fun TextStyle.display(weight: FontWeight) = copy(fontFamily = display, fontWeight = weight, letterSpacing = DisplayLetterSpacing)
    fun TextStyle.body(weight: FontWeight) = copy(fontFamily = body, fontWeight = weight)
    return Typography(
        displayLarge = base.displayLarge.display(FontWeight.ExtraBold),
        displayMedium = base.displayMedium.display(FontWeight.ExtraBold),
        displaySmall = base.displaySmall.display(FontWeight.ExtraBold),
        headlineLarge = base.headlineLarge.display(FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.display(FontWeight.ExtraBold),
        headlineSmall = base.headlineSmall.display(FontWeight.Bold),
        titleLarge = base.titleLarge.display(FontWeight.Bold),
        titleMedium = base.titleMedium.body(FontWeight.SemiBold),
        titleSmall = base.titleSmall.body(FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.body(FontWeight.Normal),
        bodyMedium = base.bodyMedium.body(FontWeight.Normal),
        bodySmall = base.bodySmall.body(FontWeight.Normal),
        labelLarge = base.labelLarge.body(FontWeight.SemiBold),
        labelMedium = base.labelMedium.body(FontWeight.SemiBold),
        labelSmall = base.labelSmall.body(FontWeight.Medium),
    )
}
