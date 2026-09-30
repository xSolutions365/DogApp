package com.example.dogapp.app.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.dogapp.feature.explore.presentation.ExploreScreen
import com.example.dogapp.feature.home.presentation.HomeScreen
import com.example.dogapp.feature.profile.presentation.ProfileScreen
import com.example.dogapp.app.navigation.ExploreRoute
import com.example.dogapp.app.navigation.HomeRoute
import com.example.dogapp.app.navigation.NavigationChange
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.app.navigation.Navigator
import com.example.dogapp.app.navigation.ProfileRoute
import com.example.dogapp.app.navigation.TopLevelDestination
import com.example.dogapp.app.navigation.platformNavigationStyle
import com.example.dogapp.app.navigation.popTransition
import com.example.dogapp.app.navigation.pushTransition
import com.example.dogapp.app.navigation.rememberNavigationState
import com.example.dogapp.app.navigation.tabSelectionTransition

@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    navigationStyle: NavigationStyle = platformNavigationStyle,
) {
    val navigationState = rememberNavigationState()
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val provideEntry = entryProvider<NavKey> {
        entry<HomeRoute> { HomeScreen() }
        entry<ExploreRoute> { ExploreScreen() }
        entry<ProfileRoute> { ProfileScreen() }
    }
    val entriesByDestination = TopLevelDestination.entries.associateWith { destination ->
        rememberDecoratedNavEntries(
            backStack = navigationState.backStacks.getValue(destination),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = provideEntry,
        )
    }
    val visibleEntries = navigationState.visibleDestinations(navigationStyle)
        .flatMap { destination -> entriesByDestination.getValue(destination) }
    val pushTransition = navigationStyle.pushTransition()
    val popTransition = navigationStyle.popTransition()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            when (navigationStyle) {
                NavigationStyle.Material -> MaterialNavigationBar(
                    selectedDestination = navigationState.selectedDestination,
                    onDestinationSelected = navigator::selectDestination,
                )
                NavigationStyle.Cupertino -> CupertinoTabBar(
                    selectedDestination = navigationState.selectedDestination,
                    onDestinationSelected = navigator::selectDestination,
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            entries = visibleEntries,
            modifier = Modifier.padding(innerPadding),
            onBack = { navigator.goBack() },
            transitionSpec = {
                if (navigationState.lastChange == NavigationChange.TabSelection) tabSelectionTransition() else pushTransition()
            },
            popTransitionSpec = {
                if (navigationState.lastChange == NavigationChange.TabSelection) tabSelectionTransition() else popTransition()
            },
            predictivePopTransitionSpec = { _ -> popTransition() },
        )
    }
}
