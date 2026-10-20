package com.example.dogapp.app.shell

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.example.dogapp.app.navigation.NavigationStyle

@Composable
actual fun rememberDonateLauncher(
    navigationStyle: NavigationStyle,
    snackbarHostState: SnackbarHostState,
    onOpenWallet: () -> Unit,
): (String) -> Unit = rememberMaterialDonateSheet(snackbarHostState, onOpenWallet)
