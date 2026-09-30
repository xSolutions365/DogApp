package com.example.dogapp.app.navigation

enum class NavigationStyle(val backFromTabRootReturnsToStartDestination: Boolean) {
    Material(backFromTabRootReturnsToStartDestination = true),
    Cupertino(backFromTabRootReturnsToStartDestination = false),
}

expect val platformNavigationStyle: NavigationStyle
