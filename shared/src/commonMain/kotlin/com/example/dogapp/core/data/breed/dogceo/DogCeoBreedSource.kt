package com.example.dogapp.core.data.breed.dogceo

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

class DogCeoBreedSource(
    private val httpClient: HttpClient,
) : BreedSource,
    BreedImageSource {
    override val id: BreedSourceId = ID
    override val displayName: String = DISPLAY_NAME
    override val precedence: Int = 2

    override suspend fun fetchBreeds(): Outcome<List<SourcedBreed>, DataError.Remote> =
        safeCall<DogCeoBreedListDto> { httpClient.get("$BASE_URL/breeds/list/all") }
            .map { dto -> dto.toSourcedBreeds() }

    private fun DogCeoBreedListDto.toSourcedBreeds(): List<SourcedBreed> =
        message
            .flatMap { (breed, subBreeds) ->
                if (subBreeds.isEmpty()) {
                    listOf(sourcedBreed(resolve(BreedNameNormalizer.normalize(breed)), reference = breed))
                } else {
                    subBreeds.map { subBreed ->
                        sourcedBreed(resolve(BreedNameNormalizer.normalize(breed, subBreed)), reference = "$breed/$subBreed")
                    }
                }
            }.distinctBy { it.normalizedName }

    private fun resolve(normalizedName: String): String = dogCeoBreedAliases[normalizedName] ?: normalizedName

    override suspend fun fetchImages(sourceReference: String): Outcome<List<String>, DataError.Remote> =
        safeCall<DogCeoImagesDto> { httpClient.get("$BASE_URL/breed/$sourceReference/images") }
            .map { dto -> dto.message.take(GALLERY_IMAGE_LIMIT) }

    private fun sourcedBreed(
        normalizedName: String,
        reference: String,
    ) = SourcedBreed(
        source = id,
        normalizedName = normalizedName,
        sourceReference = reference,
        displayName = normalizedName.split(' ').joinToString(" ") { word -> word.replaceFirstChar(Char::uppercaseChar) },
    )

    companion object {
        val ID = BreedSourceId("dog-ceo")
        const val DISPLAY_NAME = "Dog CEO"
        const val BASE_URL = "https://dog.ceo/api"
    }
}
