package com.example.dogapp.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dogapp.ui.components.TopLevelScreen
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.destination_home
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    TopLevelScreen(title = stringResource(Res.string.destination_home), modifier = modifier)
}
