package com.example.dogapp.core.data.breed.thedogapi

import com.example.dogapp.core.data.breed.BreedImageSource
import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.breed.GALLERY_IMAGE_LIMIT
import com.example.dogapp.core.data.network.safeCall
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedNameNormalizer
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.SourcedBreed
import com.example.dogapp.core.domain.map
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class TheDogApiBreedSource(
    private val httpClient: HttpClient,
    private val apiKey: String,
) : BreedSource,
    BreedImageSource {
    override val id: BreedSourceId = ID
    override val displayName: String = DISPLAY_NAME
    override val precedence: Int = 0

    override suspend fun fetchBreeds(): Outcome<List<SourcedBreed>, DataError.Remote> =
        safeCall<List<TheDogApiBreedDto>> {
            httpClient.get("$BASE_URL/breeds") { header(API_KEY_HEADER, apiKey) }
        }.map { breeds -> breeds.map { it.toSourcedBreed() }.distinctBy { it.normalizedName } }

    override suspend fun fetchImages(sourceReference: String): Outcome<List<String>, DataError.Remote> =
        safeCall<List<TheDogApiSearchImageDto>> {
            httpClient.get("$BASE_URL/images/search") {
                header(API_KEY_HEADER, apiKey)
                parameter("breed_ids", sourceReference)
                parameter("limit", GALLERY_IMAGE_LIMIT)
            }
        }.map { images -> images.map { it.url } }

    private fun TheDogApiBreedDto.toSourcedBreed() =
        SourcedBreed(
            source = this@TheDogApiBreedSource.id,
            normalizedName = BreedNameNormalizer.normalize(name),
            displayName = name,
            sourceReference = id,
            weightKg = parseTheDogApiRange(weight?.metric),
            heightCm = parseTheDogApiRange(height?.metric),
            lifeSpanYears = parseTheDogApiRange(lifeSpan),
            temperament =
                temperament
                    ?.split(',')
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    .orEmpty(),
            breedGroup = breedGroup,
            origin = origin,
            description = description,
            history = history,
            imageUrls = listOfNotNull(image?.url),
        )

    companion object {
        val ID = BreedSourceId("the-dog-api")
        const val DISPLAY_NAME = "TheDogAPI"
        const val BASE_URL = "https://api.thedogapi.com/v1"
        const val API_KEY_HEADER = "x-api-key"
    }
}
