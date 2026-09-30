package com.example.dogapp.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dogapp.ui.components.TopLevelScreen
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.destination_explore
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    TopLevelScreen(title = stringResource(Res.string.destination_explore), modifier = modifier)
}
