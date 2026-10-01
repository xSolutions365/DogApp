package com.example.dogapp.core.data.breed.dogceo

import com.example.dogapp.core.data.network.HttpClientFactory
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.SourcedBreed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DogCeoBreedSourceTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun breedListMapsBreedsAndSubBreedsToNormalizedAliasResolvedSourcedBreeds() =
        runTest {
            val requestedUrls = mutableListOf<String>()
            val engine =
                MockEngine { request ->
                    requestedUrls += request.url.toString()
                    respond(
                        """{"message":{"beagle":[],"chow":[],"collie":["border"],"hound":["afghan","basset"],"kelpie":[],"australian":["kelpie"]},"status":"success"}""",
                        HttpStatusCode.OK,
                        jsonHeaders,
                    )
                }
            val source = DogCeoBreedSource(HttpClientFactory.create(engine))

            val result = source.fetchBreeds()

            assertEquals(listOf("https://dog.ceo/api/breeds/list/all"), requestedUrls)
            assertEquals(
                Outcome.Success(
                    listOf(
                        SourcedBreed(DogCeoBreedSource.ID, normalizedName = "beagle", displayName = "Beagle"),
                        SourcedBreed(DogCeoBreedSource.ID, normalizedName = "chow chow", displayName = "Chow Chow"),
                        SourcedBreed(DogCeoBreedSource.ID, normalizedName = "border collie", displayName = "Border Collie"),
                        SourcedBreed(DogCeoBreedSource.ID, normalizedName = "afghan hound", displayName = "Afghan Hound"),
                        SourcedBreed(DogCeoBreedSource.ID, normalizedName = "basset hound", displayName = "Basset Hound"),
                        SourcedBreed(DogCeoBreedSource.ID, normalizedName = "australian kelpie", displayName = "Australian Kelpie"),
                    ),
                ),
                result,
            )
        }

    @Test
    fun errorResponsesMapToRemoteDataErrors() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        """{"status":"error","message":"Breed not found (main breed does not exist)","code":404}""",
                        HttpStatusCode.NotFound,
                        jsonHeaders,
                    )
                }

            assertEquals(Outcome.Failure(DataError.Remote.NotFound), DogCeoBreedSource(HttpClientFactory.create(engine)).fetchBreeds())
        }
}
