package com.example.dogapp

import androidx.compose.runtime.Composable
import com.example.dogapp.app.shell.AppShell
import com.example.dogapp.core.presentation.theme.DogAppTheme

@Composable
fun App() {
    DogAppTheme {
        AppShell()
    }
}
