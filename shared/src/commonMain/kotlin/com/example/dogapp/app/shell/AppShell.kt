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
import com.example.dogapp.app.navigation.NavigationChange
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.app.navigation.Navigator
import com.example.dogapp.app.navigation.TopLevelDestination
import com.example.dogapp.app.navigation.platformNavigationStyle
import com.example.dogapp.app.navigation.popTransition
import com.example.dogapp.app.navigation.pushTransition
import com.example.dogapp.app.navigation.rememberNavigationState
import com.example.dogapp.app.navigation.tabSelectionTransition
import com.example.dogapp.core.presentation.animation.SharedElementsHost
import com.example.dogapp.feature.breeddetail.presentation.BreedDetailRoute
import com.example.dogapp.feature.breeddetail.presentation.breedDetailEntries
import com.example.dogapp.feature.explore.presentation.exploreEntries
import com.example.dogapp.feature.home.presentation.homeEntries
import com.example.dogapp.feature.identify.presentation.IdentifyRoute
import com.example.dogapp.feature.identify.presentation.identifyEntries
import com.example.dogapp.feature.profile.presentation.profileEntries

@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    navigationStyle: NavigationStyle = platformNavigationStyle,
) {
    val navigationState = rememberNavigationState()
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val provideEntry =
        entryProvider<NavKey> {
            homeEntries(
                onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) },
                onIdentifyClick = { navigator.navigate(IdentifyRoute) },
            )
            exploreEntries(onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) })
            profileEntries(onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) })
            breedDetailEntries(onBack = { navigator.goBack() })
            identifyEntries(onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) }, onBack = { navigator.goBack() })
        }
    val entriesByDestination =
        TopLevelDestination.entries.associateWith { destination ->
            rememberDecoratedNavEntries(
                backStack = navigationState.backStacks.getValue(destination),
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider = provideEntry,
            )
        }
    val visibleEntries =
        navigationState
            .visibleDestinations(navigationStyle)
            .flatMap { destination -> entriesByDestination.getValue(destination) }
    val pushTransition = navigationStyle.pushTransition()
    val popTransition = navigationStyle.popTransition()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            when (navigationStyle) {
                NavigationStyle.Material -> {
                    MaterialNavigationBar(
                        selectedDestination = navigationState.selectedDestination,
                        onDestinationSelect = navigator::selectDestination,
                    )
                }

                NavigationStyle.Cupertino -> {
                    CupertinoTabBar(
                        selectedDestination = navigationState.selectedDestination,
                        onDestinationSelect = navigator::selectDestination,
                    )
                }
            }
        },
    ) { innerPadding ->
        SharedElementsHost(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                entries = visibleEntries,
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
}
