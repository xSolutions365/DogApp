package com.example.dogapp.navigation

enum class NavigationStyle(val backFromTabRootReturnsToStartDestination: Boolean) {
    Material(backFromTabRootReturnsToStartDestination = true),
    Cupertino(backFromTabRootReturnsToStartDestination = false),
}

expect val platformNavigationStyle: NavigationStyle
