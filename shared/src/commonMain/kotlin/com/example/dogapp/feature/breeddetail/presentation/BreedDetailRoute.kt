package com.example.dogapp.feature.breeddetail.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data class BreedDetailRoute(
    val breedId: String,
) : NavKey

val breedDetailNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(BreedDetailRoute::class, BreedDetailRoute.serializer())
        }
    }
