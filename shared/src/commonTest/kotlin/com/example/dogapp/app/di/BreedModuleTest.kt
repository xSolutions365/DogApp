package com.example.dogapp.app.di

import com.example.dogapp.core.data.breed.BreedImageSource
import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.breed.apininjas.ApiNinjasBreedSource
import com.example.dogapp.core.data.breed.dogceo.DogCeoBreedSource
import com.example.dogapp.core.data.breed.thedogapi.TheDogApiBreedSource
import com.example.dogapp.core.data.network.HttpClientFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class BreedModuleTest {
    private fun registeredSources(
        theDogApiKey: String?,
        apiNinjasKey: String?,
    ): Pair<Set<String>, Set<String>> {
        val koin =
            koinApplication {
                modules(
                    breedModule(theDogApiKey = theDogApiKey, apiNinjasKey = apiNinjasKey),
                    module { single { HttpClientFactory.create(MockEngine { respondOk() }) } },
                )
            }.koin
        return koin.getAll<BreedSource>().map { it.id.value }.toSet() to
            koin.getAll<BreedImageSource>().map { it.id.value }.toSet()
    }

    @Test
    fun dogCeoIsAlwaysRegisteredAndEachKeyedSourceOnlyWhenItsKeyIsSet() {
        val dogCeo = DogCeoBreedSource.ID.value
        val theDogApi = TheDogApiBreedSource.ID.value
        val apiNinjas = ApiNinjasBreedSource.ID.value

        assertEquals(setOf(dogCeo) to setOf(dogCeo), registeredSources(theDogApiKey = null, apiNinjasKey = null))
        assertEquals(
            setOf(dogCeo, theDogApi) to setOf(dogCeo, theDogApi),
            registeredSources(theDogApiKey = "dog-key", apiNinjasKey = null),
        )
        assertEquals(
            setOf(dogCeo, apiNinjas) to setOf(dogCeo),
            registeredSources(theDogApiKey = null, apiNinjasKey = "ninjas-key"),
        )
        assertEquals(
            setOf(dogCeo, theDogApi, apiNinjas) to setOf(dogCeo, theDogApi),
            registeredSources(theDogApiKey = "dog-key", apiNinjasKey = "ninjas-key"),
        )
    }
}
