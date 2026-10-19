package com.example.dogapp.app.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
import com.example.dogapp.feature.donate.presentation.CharitiesRoute
import com.example.dogapp.feature.donate.presentation.CharityRoute
import com.example.dogapp.feature.donate.presentation.donateEntries
import com.example.dogapp.feature.explore.presentation.exploreEntries
import com.example.dogapp.feature.home.presentation.HomeLayout
import com.example.dogapp.feature.home.presentation.homeEntries
import com.example.dogapp.feature.identify.presentation.IdentifyRoute
import com.example.dogapp.feature.identify.presentation.identifyEntries
import com.example.dogapp.feature.profile.presentation.profileEntries
import com.example.dogapp.feature.wallet.domain.WalletAutoLock
import com.example.dogapp.feature.wallet.presentation.AddCardRoute
import com.example.dogapp.feature.wallet.presentation.CardDetailRoute
import com.example.dogapp.feature.wallet.presentation.WalletLayout
import com.example.dogapp.feature.wallet.presentation.WalletRoute
import com.example.dogapp.feature.wallet.presentation.walletEntries
import org.koin.compose.koinInject

@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    navigationStyle: NavigationStyle = platformNavigationStyle,
    walletAutoLock: WalletAutoLock = koinInject(),
) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { walletAutoLock.onAppBackground() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { walletAutoLock.onAppForeground() }
    val navigationState = rememberNavigationState()
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val launchIdentify =
        rememberIdentifyLauncher(
            navigationStyle = navigationStyle,
            navigateToIdentify = { navigator.navigate(IdentifyRoute) },
            onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) },
        )

    val launchAddCard =
        rememberAddCardLauncher(
            navigationStyle = navigationStyle,
            navigateToAddCard = { navigator.navigate(AddCardRoute) },
        )

    val provideEntry =
        entryProvider<NavKey> {
            homeEntries(
                layout =
                    when (navigationStyle) {
                        NavigationStyle.Material -> HomeLayout.Material
                        NavigationStyle.Cupertino -> HomeLayout.Cupertino
                    },
                onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) },
                onIdentifyClick = launchIdentify,
            )
            exploreEntries(onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) })
            profileEntries(
                onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) },
                onSupportCharitiesClick = { navigator.navigate(CharitiesRoute) },
                onWalletClick = { navigator.navigate(WalletRoute) },
            )
            breedDetailEntries(onBack = { navigator.goBack() })
            identifyEntries(onBreedClick = { breedId -> navigator.navigate(BreedDetailRoute(breedId)) }, onBack = { navigator.goBack() })
            donateEntries(onCharityClick = { charityId -> navigator.navigate(CharityRoute(charityId)) }, onBack = { navigator.goBack() })
            walletEntries(
                layout =
                    when (navigationStyle) {
                        NavigationStyle.Material -> WalletLayout.Material
                        NavigationStyle.Cupertino -> WalletLayout.Cupertino
                    },
                onAddCardClick = launchAddCard,
                onCardClick = { cardId -> navigator.navigate(CardDetailRoute(cardId)) },
                onBack = { navigator.goBack() },
            )
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
