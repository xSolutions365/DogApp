package com.example.dogapp.feature.profile.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object ProfileRoute : NavKey

@Serializable
data object FavouritesRoute : NavKey

@Serializable
data object DrawHistoryRoute : NavKey

@Serializable
data object DeviceStateRoute : NavKey

val profileNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(FavouritesRoute::class, FavouritesRoute.serializer())
            subclass(DrawHistoryRoute::class, DrawHistoryRoute.serializer())
            subclass(DeviceStateRoute::class, DeviceStateRoute.serializer())
        }
    }
