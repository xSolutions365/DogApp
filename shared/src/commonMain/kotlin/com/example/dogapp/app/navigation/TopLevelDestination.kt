package com.example.dogapp.app.navigation

import androidx.navigation3.runtime.NavKey
import com.example.dogapp.feature.explore.presentation.ExploreRoute
import com.example.dogapp.feature.home.presentation.HomeRoute
import com.example.dogapp.feature.profile.presentation.ProfileRoute
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.destination_explore
import dogapp.shared.generated.resources.destination_home
import dogapp.shared.generated.resources.destination_profile
import dogapp.shared.generated.resources.ic_explore_filled
import dogapp.shared.generated.resources.ic_explore_outlined
import dogapp.shared.generated.resources.ic_home_filled
import dogapp.shared.generated.resources.ic_home_outlined
import dogapp.shared.generated.resources.ic_profile_filled
import dogapp.shared.generated.resources.ic_profile_outlined
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
    val route: NavKey,
    val label: StringResource,
    val selectedIcon: DrawableResource,
    val unselectedIcon: DrawableResource,
) {
    Home(
        route = HomeRoute,
        label = Res.string.destination_home,
        selectedIcon = Res.drawable.ic_home_filled,
        unselectedIcon = Res.drawable.ic_home_outlined,
    ),
    Explore(
        route = ExploreRoute,
        label = Res.string.destination_explore,
        selectedIcon = Res.drawable.ic_explore_filled,
        unselectedIcon = Res.drawable.ic_explore_outlined,
    ),
    Profile(
        route = ProfileRoute,
        label = Res.string.destination_profile,
        selectedIcon = Res.drawable.ic_profile_filled,
        unselectedIcon = Res.drawable.ic_profile_outlined,
    ),
}
