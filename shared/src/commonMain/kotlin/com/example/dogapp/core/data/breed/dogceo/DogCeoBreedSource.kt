package com.example.dogapp.core.data.breed.dogceo

import com.example.dogapp.core.data.breed.BreedNameNormalizer
import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.network.safeCall
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.SourcedBreed
import com.example.dogapp.core.domain.map
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class DogCeoBreedSource(
    private val httpClient: HttpClient,
) : BreedSource {
    override val id: BreedSourceId = ID

    override suspend fun fetchBreeds(): Outcome<List<SourcedBreed>, DataError.Remote> =
        safeCall<DogCeoBreedListDto> { httpClient.get("$BASE_URL/breeds/list/all") }
            .map { dto -> dto.toSourcedBreeds() }

    private fun DogCeoBreedListDto.toSourcedBreeds(): List<SourcedBreed> =
        message.flatMap { (breed, subBreeds) ->
            if (subBreeds.isEmpty()) {
                listOf(sourcedBreed(BreedNameNormalizer.normalize(breed)))
            } else {
                subBreeds.map { subBreed -> sourcedBreed(BreedNameNormalizer.normalize(breed, subBreed)) }
            }
        }

    private fun sourcedBreed(normalizedName: String) =
        SourcedBreed(
            source = id,
            normalizedName = normalizedName,
            displayName = normalizedName.split(' ').joinToString(" ") { word -> word.replaceFirstChar(Char::uppercaseChar) },
        )

    companion object {
        val ID = BreedSourceId("dog-ceo")
        const val BASE_URL = "https://dog.ceo/api"
    }
}
