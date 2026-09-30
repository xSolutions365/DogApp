package com.example.dogapp.app.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigatorTest {

    @Serializable
    private data object DetailRoute : NavKey

    private val state = NavigationState(
        startDestination = TopLevelDestination.Home,
        selectedDestination = mutableStateOf(TopLevelDestination.Home),
        backStacks = TopLevelDestination.entries.associateWith { NavBackStack(it.route) },
    )
    private val navigator = Navigator(state)

    @Test
    fun startsOnStartDestinationWithEveryTabAtItsRoot() {
        assertEquals(TopLevelDestination.Home, state.selectedDestination)
        assertEquals(NavigationChange.StackChange, state.lastChange)
        TopLevelDestination.entries.forEach { destination ->
            assertEquals(listOf(destination.route), state.backStacks.getValue(destination).toList())
        }
    }

    @Test
    fun navigatingToTopLevelRouteSwitchesTabAndOtherRoutesPushOntoCurrentTab() {
        navigator.navigate(ExploreRoute)
        assertEquals(NavigationChange.TabSelection, state.lastChange)

        navigator.navigate(DetailRoute)
        assertEquals(NavigationChange.StackChange, state.lastChange)

        assertEquals(TopLevelDestination.Explore, state.selectedDestination)
        assertEquals(listOf(ExploreRoute, DetailRoute), state.currentBackStack.toList())
        assertEquals(listOf<NavKey>(HomeRoute), state.backStacks.getValue(TopLevelDestination.Home).toList())
    }

    @Test
    fun eachTabKeepsItsOwnBackStackAcrossTabSwitches() {
        navigator.navigate(DetailRoute)
        navigator.selectDestination(TopLevelDestination.Profile)
        navigator.selectDestination(TopLevelDestination.Home)

        assertEquals(listOf(HomeRoute, DetailRoute), state.currentBackStack.toList())
    }

    @Test
    fun reselectingCurrentTabPopsItToRoot() {
        navigator.selectDestination(TopLevelDestination.Explore)
        navigator.navigate(DetailRoute)
        navigator.navigate(DetailRoute)

        navigator.selectDestination(TopLevelDestination.Explore)

        assertEquals(TopLevelDestination.Explore, state.selectedDestination)
        assertEquals(listOf<NavKey>(ExploreRoute), state.currentBackStack.toList())
        assertEquals(NavigationChange.StackChange, state.lastChange)
    }

    @Test
    fun goBackPopsWithinTabThenReturnsToStartDestinationThenIsUnhandled() {
        navigator.selectDestination(TopLevelDestination.Profile)
        navigator.navigate(DetailRoute)

        assertTrue(navigator.goBack())
        assertEquals(listOf<NavKey>(ProfileRoute), state.currentBackStack.toList())

        assertTrue(navigator.goBack())
        assertEquals(TopLevelDestination.Home, state.selectedDestination)
        assertEquals(NavigationChange.StackChange, state.lastChange)

        assertFalse(navigator.goBack())
        assertEquals(TopLevelDestination.Home, state.selectedDestination)
    }

    @Test
    fun visibleDestinationsFollowPlatformBackConvention() {
        assertEquals(listOf(TopLevelDestination.Home), state.visibleDestinations(NavigationStyle.Material))
        assertEquals(listOf(TopLevelDestination.Home), state.visibleDestinations(NavigationStyle.Cupertino))

        navigator.selectDestination(TopLevelDestination.Explore)

        assertEquals(
            listOf(TopLevelDestination.Home, TopLevelDestination.Explore),
            state.visibleDestinations(NavigationStyle.Material),
        )
        assertEquals(listOf(TopLevelDestination.Explore), state.visibleDestinations(NavigationStyle.Cupertino))
    }
}
