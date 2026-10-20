package com.example.dogapp.feature.donate.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object CharitiesRoute : NavKey

@Serializable
data object DonationsRoute : NavKey

@Serializable
data class CharityRoute(
    val charityId: String,
) : NavKey

val donateNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CharitiesRoute::class, CharitiesRoute.serializer())
            subclass(CharityRoute::class, CharityRoute.serializer())
            subclass(DonationsRoute::class, DonationsRoute.serializer())
        }
    }
