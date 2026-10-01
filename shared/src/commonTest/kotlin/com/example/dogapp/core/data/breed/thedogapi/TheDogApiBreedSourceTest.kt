package com.example.dogapp.core.data.breed.thedogapi

import com.example.dogapp.core.data.network.HttpClientFactory
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.SourcedBreed
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TheDogApiBreedSourceTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private val breedsJson =
        """
        [
          {"id":"50","name":"Border Collie","species_id":"2","life_span":"12-15",
           "temperament":"Intelligent, energetic, alert, work-focused, loyal, eager to please",
           "origin":"Anglo-Scottish border region","country_code":"GB","description":"Medium-sized herding dog.",
           "bred_for":null,"perfect_for":null,"breed_group":"Herding","history":"Developed by shepherds.",
           "reference_image_id":"38KoA9gGB",
           "weight":{"imperial":"30-55","metric":"14-25"},
           "height":{"imperial":"Male: 19-22; Female: 18-21","metric":"Male: 48-56; Female: 46-53"},
           "image":{"id":"38KoA9gGB","url":"https://cdn2.thedogapi.com/images/38KoA9gGB.jpg","width":1080,"height":1350}},
          {"id":"51","name":"Border-Collie","life_span":null,"temperament":"","origin":"GB","breed_group":"Herding",
           "description":"Duplicate entry.","history":"Duplicate.",
           "weight":{"imperial":"unknown","metric":"unknown"},"height":{"imperial":"up to 22","metric":"up to 56"},
           "image":{"id":"x","url":"https://cdn2.thedogapi.com/images/x.jpg","width":1,"height":1}},
          {"id":"60","name":"Löwchen","life_span":null,"temperament":"Playful","origin":"France","breed_group":"Toy",
           "description":"Little lion dog.","history":"Old breed.",
           "weight":{"imperial":"unknown","metric":"unknown"},"height":{"imperial":"up to 14","metric":"up to 36"},
           "image":{"id":"y","url":"https://cdn2.thedogapi.com/images/y.jpg","width":1,"height":1}}
        ]
        """.trimIndent()

    @Test
    fun breedsAreFetchedWithTheApiKeyAndMappedToMetricSourcedBreeds() =
        runTest {
            val requests = mutableListOf<HttpRequestData>()
            val engine =
                MockEngine { request ->
                    requests += request
                    respond(breedsJson, HttpStatusCode.OK, jsonHeaders)
                }

            val result = TheDogApiBreedSource(HttpClientFactory.create(engine), apiKey = "test-key").fetchBreeds()

            val request = requests.single()
            assertEquals("https://api.thedogapi.com/v1/breeds", request.url.toString())
            assertEquals("test-key", request.headers[TheDogApiBreedSource.API_KEY_HEADER])
            assertEquals(
                Outcome.Success(
                    listOf(
                        SourcedBreed(
                            source = TheDogApiBreedSource.ID,
                            normalizedName = "border collie",
                            displayName = "Border Collie",
                            weightKg = MeasureRange(14.0, 25.0),
                            heightCm = MeasureRange(46.0, 56.0),
                            lifeSpanYears = MeasureRange(12.0, 15.0),
                            temperament = listOf("Intelligent", "energetic", "alert", "work-focused", "loyal", "eager to please"),
                            breedGroup = "Herding",
                            origin = "Anglo-Scottish border region",
                            description = "Medium-sized herding dog.",
                            history = "Developed by shepherds.",
                            imageUrls = listOf("https://cdn2.thedogapi.com/images/38KoA9gGB.jpg"),
                        ),
                        SourcedBreed(
                            source = TheDogApiBreedSource.ID,
                            normalizedName = "lowchen",
                            displayName = "Löwchen",
                            temperament = listOf("Playful"),
                            breedGroup = "Toy",
                            origin = "France",
                            description = "Little lion dog.",
                            history = "Old breed.",
                            imageUrls = listOf("https://cdn2.thedogapi.com/images/y.jpg"),
                        ),
                    ),
                ),
                result,
            )
        }

    @Test
    fun rejectedKeyMapsToUnauthorizedOrClientError() =
        runTest {
            val forbidden =
                MockEngine {
                    respond(
                        """{"statusCode":403,"message":"Authentication required. Please provide a valid API key.","error":"Forbidden"}""",
                        HttpStatusCode.Forbidden,
                        jsonHeaders,
                    )
                }

            assertEquals(
                Outcome.Failure(DataError.Remote.Client(403)),
                TheDogApiBreedSource(HttpClientFactory.create(forbidden), apiKey = "bad").fetchBreeds(),
            )
        }
}
