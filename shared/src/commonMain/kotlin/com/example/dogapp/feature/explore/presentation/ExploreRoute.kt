package com.example.dogapp.feature.explore.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object ExploreRoute : NavKey

val exploreNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ExploreRoute::class, ExploreRoute.serializer())
        }
    }
