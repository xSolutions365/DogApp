package com.example.dogapp.app.shell

import androidx.compose.runtime.Composable
import com.example.dogapp.app.navigation.NavigationStyle

@Composable
actual fun rememberIdentifyLauncher(
    navigationStyle: NavigationStyle,
    navigateToIdentify: () -> Unit,
    onBreedClick: (String) -> Unit,
): () -> Unit = navigateToIdentify
