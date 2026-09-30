package com.example.dogapp.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class NavigationState(
    val startDestination: TopLevelDestination,
    selectedDestination: MutableState<TopLevelDestination>,
    val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
) {
    var selectedDestination: TopLevelDestination by selectedDestination

    var lastChange: NavigationChange by mutableStateOf(NavigationChange.StackChange)

    val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(selectedDestination)

    fun visibleDestinations(style: NavigationStyle): List<TopLevelDestination> =
        if (style.backFromTabRootReturnsToStartDestination && selectedDestination != startDestination) {
            listOf(startDestination, selectedDestination)
        } else {
            listOf(selectedDestination)
        }
}

private val navigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(ExploreRoute::class, ExploreRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
        }
    }
}

@Composable
fun rememberNavigationState(
    startDestination: TopLevelDestination = TopLevelDestination.Home,
): NavigationState {
    val selectedDestination = rememberSaveable { mutableStateOf(startDestination) }
    val backStacks = TopLevelDestination.entries.associateWith { destination ->
        rememberNavBackStack(navigationSavedStateConfiguration, destination.route)
    }
    return remember { NavigationState(startDestination, selectedDestination, backStacks) }
}
