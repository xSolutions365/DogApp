package com.example.dogapp.feature.profile.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object ProfileRoute : NavKey

@Serializable
data object DeviceChecksRoute : NavKey

val profileNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(DeviceChecksRoute::class, DeviceChecksRoute.serializer())
        }
    }
