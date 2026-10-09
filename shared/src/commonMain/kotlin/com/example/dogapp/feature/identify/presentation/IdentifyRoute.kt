package com.example.dogapp.feature.identify.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object IdentifyRoute : NavKey

val identifyNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(IdentifyRoute::class, IdentifyRoute.serializer())
        }
    }
