package com.example.dogapp.core.data.breed.apininjas

import com.example.dogapp.core.data.network.HttpClientFactory
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedTrait
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ApiNinjasBreedSourceTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun dogJson(name: String) =
        """
        {"image_link":"https://api-ninjas.com/images/dogs/border_collie.jpg","good_with_children":3,"good_with_other_dogs":3,
         "shedding":3,"grooming":3,"drooling":1,"coat_length":1,"good_with_strangers":4,"playfulness":5,"protectiveness":3,
         "trainability":5,"energy":5,"barking":4,"min_life_expectancy":12.0,"max_life_expectancy":15.0,
         "max_height_male":22.0,"max_height_female":21.0,"max_weight_male":55.0,"max_weight_female":50.0,
         "min_height_male":19.0,"min_height_female":18.0,"min_weight_male":30.0,"min_weight_female":27.0,"name":"$name"}
        """.trimIndent()

    private fun page(names: List<String>) = names.joinToString(prefix = "[", postfix = "]") { dogJson(it) }

    @Test
    fun pagesUntilAShortPageAndMapsToMetricAliasResolvedSourcedBreeds() =
        runTest {
            val requests = mutableListOf<HttpRequestData>()
            val fullPage = (1..ApiNinjasBreedSource.PAGE_SIZE).map { "Breed $it" }
            val engine =
                MockEngine { request ->
                    requests += request
                    val body =
                        if (request.url.parameters["offset"] ==
                            "0"
                        ) {
                            page(fullPage)
                        } else {
                            page(listOf("Border Collie", "German Shepherd Dog"))
                        }
                    respond(body, HttpStatusCode.OK, jsonHeaders)
                }

            val result = ApiNinjasBreedSource(HttpClientFactory.create(engine), apiKey = "test-key").fetchBreeds()

            assertEquals(listOf("0", "20"), requests.map { it.url.parameters["offset"] })
            requests.forEach { request ->
                assertEquals("https://api.api-ninjas.com/v1/dogs", request.url.toString().substringBefore('?'))
                assertEquals("test-key", request.headers[ApiNinjasBreedSource.API_KEY_HEADER])
                assertEquals("1", request.url.parameters["min_life_expectancy"])
            }
            val breeds = assertIs<Outcome.Success<List<SourcedBreed>>>(result).data
            assertEquals(ApiNinjasBreedSource.PAGE_SIZE + 2, breeds.size)
            assertEquals("German Shepherd Dog", breeds.single { it.normalizedName == "german shepherd" }.displayName)
            val borderCollie = breeds.single { it.normalizedName == "border collie" }
            assertEquals("Border Collie", borderCollie.displayName)
            val weightKg = assertNotNull(borderCollie.weightKg)
            val heightCm = assertNotNull(borderCollie.heightCm)
            val lifeSpanYears = assertNotNull(borderCollie.lifeSpanYears)
            assertEquals(12.25, weightKg.min, 0.01)
            assertEquals(24.95, weightKg.max, 0.01)
            assertEquals(45.72, heightCm.min, 0.01)
            assertEquals(55.88, heightCm.max, 0.01)
            assertEquals(12.0, lifeSpanYears.min)
            assertEquals(15.0, lifeSpanYears.max)
            assertEquals(BreedTrait.entries.toSet(), borderCollie.traits.keys)
            assertEquals(5, borderCollie.traits[BreedTrait.Trainability])
            assertEquals(1, borderCollie.traits[BreedTrait.Drooling])
            assertEquals(listOf("https://api-ninjas.com/images/dogs/border_collie.jpg"), borderCollie.imageUrls)
        }

    @Test
    fun failureOnAnyPageFailsTheSourceAndEndlessPagingIsCapped() =
        runTest {
            val failingSecondPage =
                MockEngine { request ->
                    if (request.url.parameters["offset"] == "0") {
                        respond(page((1..ApiNinjasBreedSource.PAGE_SIZE).map { "Breed $it" }), HttpStatusCode.OK, jsonHeaders)
                    } else {
                        respond("""{"error": "Missing API Key."}""", HttpStatusCode.BadRequest, jsonHeaders)
                    }
                }
            var endlessRequests = 0
            val endless =
                MockEngine {
                    endlessRequests++
                    respond(page((1..ApiNinjasBreedSource.PAGE_SIZE).map { "Breed $it" }), HttpStatusCode.OK, jsonHeaders)
                }

            assertEquals(
                Outcome.Failure(DataError.Remote.Client(400)),
                ApiNinjasBreedSource(HttpClientFactory.create(failingSecondPage), apiKey = "test-key").fetchBreeds(),
            )
            assertEquals(
                Outcome.Failure(DataError.Remote.Unknown),
                ApiNinjasBreedSource(HttpClientFactory.create(endless), apiKey = "test-key").fetchBreeds(),
            )
            assertEquals(ApiNinjasBreedSource.MAX_PAGES, endlessRequests)
        }
}
