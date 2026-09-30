package com.example.dogapp.app.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(private val state: NavigationState) {

    fun selectDestination(destination: TopLevelDestination) {
        if (destination == state.selectedDestination) {
            state.lastChange = NavigationChange.StackChange
            popToRoot()
        } else {
            state.lastChange = NavigationChange.TabSelection
            state.selectedDestination = destination
        }
    }

    fun navigate(route: NavKey) {
        val destination = TopLevelDestination.entries.firstOrNull { it.route == route }
        if (destination != null) {
            selectDestination(destination)
        } else {
            state.lastChange = NavigationChange.StackChange
            state.currentBackStack.add(route)
        }
    }

    fun goBack(): Boolean {
        state.lastChange = NavigationChange.StackChange
        val backStack = state.currentBackStack
        return when {
            backStack.size > 1 -> {
                backStack.removeAt(backStack.lastIndex)
                true
            }
            state.selectedDestination != state.startDestination -> {
                state.selectedDestination = state.startDestination
                true
            }
            else -> false
        }
    }

    private fun popToRoot() {
        val backStack = state.currentBackStack
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}
