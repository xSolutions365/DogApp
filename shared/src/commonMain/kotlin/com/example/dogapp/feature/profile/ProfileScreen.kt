package com.example.dogapp.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dogapp.ui.components.TopLevelScreen
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.destination_profile
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    TopLevelScreen(title = stringResource(Res.string.destination_profile), modifier = modifier)
}
