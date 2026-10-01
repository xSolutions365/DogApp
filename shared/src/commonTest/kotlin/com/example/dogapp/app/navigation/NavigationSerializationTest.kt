package com.example.dogapp.app.navigation

import androidx.navigation3.runtime.NavKey
import com.example.dogapp.feature.breeddetail.presentation.BreedDetailRoute
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationSerializationTest {
    private val json = Json { serializersModule = navigationSerializersModule }
    private val navKeySerializer = PolymorphicSerializer(NavKey::class)

    @Test
    fun everyTopLevelRouteRoundTripsThroughFeatureProvidedSerializers() {
        TopLevelDestination.entries.forEach { destination ->
            val encoded = json.encodeToString(navKeySerializer, destination.route)

            assertEquals(destination.route, json.decodeFromString(navKeySerializer, encoded), "Round trip failed for $destination")
        }
    }

    @Test
    fun featureRoutesWithArgumentsRoundTripThroughFeatureProvidedSerializers() {
        val route: NavKey = BreedDetailRoute(breedId = "border collie")

        assertEquals(route, json.decodeFromString(navKeySerializer, json.encodeToString(navKeySerializer, route)))
    }
}
