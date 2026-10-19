package com.example.dogapp.app.shell

import androidx.compose.runtime.Composable
import com.example.dogapp.app.navigation.NavigationStyle

@Composable
actual fun rememberAddCardLauncher(
    navigationStyle: NavigationStyle,
    navigateToAddCard: () -> Unit,
): () -> Unit = navigateToAddCard
