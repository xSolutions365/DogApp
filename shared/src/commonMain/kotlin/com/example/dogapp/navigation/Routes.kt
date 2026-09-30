package com.example.dogapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object ExploreRoute : NavKey

@Serializable
data object ProfileRoute : NavKey
