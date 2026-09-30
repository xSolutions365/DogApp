package com.example.dogapp.feature.home.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object HomeRoute : NavKey

val homeNavigationSerializers = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(HomeRoute::class, HomeRoute.serializer())
    }
}
