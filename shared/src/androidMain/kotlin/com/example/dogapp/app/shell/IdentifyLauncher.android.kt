package com.example.dogapp.app.shell

import androidx.compose.runtime.Composable
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.feature.identify.presentation.PhotoSource

@Composable
actual fun rememberIdentifyLauncher(
    navigationStyle: NavigationStyle,
    navigateToIdentify: (PhotoSource) -> Unit,
    onBreedClick: (String) -> Unit,
): (PhotoSource) -> Unit = navigateToIdentify
