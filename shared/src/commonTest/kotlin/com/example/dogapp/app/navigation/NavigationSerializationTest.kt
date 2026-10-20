package com.example.dogapp.app.navigation

import androidx.navigation3.runtime.NavKey
import com.example.dogapp.feature.breeddetail.presentation.BreedDetailRoute
import com.example.dogapp.feature.donate.presentation.CharitiesRoute
import com.example.dogapp.feature.donate.presentation.CharityRoute
import com.example.dogapp.feature.donate.presentation.DonationsRoute
import com.example.dogapp.feature.identify.presentation.IdentifyRoute
import com.example.dogapp.feature.profile.presentation.DeviceStateRoute
import com.example.dogapp.feature.profile.presentation.DrawHistoryRoute
import com.example.dogapp.feature.profile.presentation.FavouritesRoute
import com.example.dogapp.feature.wallet.presentation.AddCardRoute
import com.example.dogapp.feature.wallet.presentation.CardDetailRoute
import com.example.dogapp.feature.wallet.presentation.WalletRoute
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
    fun pushedFeatureRoutesRoundTripThroughFeatureProvidedSerializers() {
        listOf<NavKey>(
            BreedDetailRoute(breedId = "border collie"),
            IdentifyRoute,
            CharitiesRoute,
            CharityRoute("fetch-club"),
            DonationsRoute,
            FavouritesRoute,
            DrawHistoryRoute,
            DeviceStateRoute,
            WalletRoute,
            AddCardRoute,
            CardDetailRoute("card-1"),
        ).forEach { route ->
            assertEquals(
                route,
                json.decodeFromString(navKeySerializer, json.encodeToString(navKeySerializer, route)),
                "Round trip failed for $route",
            )
        }
    }
}
