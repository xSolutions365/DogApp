package com.example.dogapp.core.data.breed.apininjas

import com.example.dogapp.core.data.breed.BreedNameNormalizer
import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.breed.inchesToCentimetres
import com.example.dogapp.core.data.breed.poundsToKilograms
import com.example.dogapp.core.data.network.safeCall
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.SourcedBreed
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class ApiNinjasBreedSource(
    private val httpClient: HttpClient,
    private val apiKey: String,
) : BreedSource {
    override val id: BreedSourceId = ID

    override suspend fun fetchBreeds(): Outcome<List<SourcedBreed>, DataError.Remote> {
        val dogs = mutableListOf<ApiNinjasDogDto>()
        repeat(MAX_PAGES) { page ->
            when (val result = fetchPage(offset = page * PAGE_SIZE)) {
                is Outcome.Failure -> {
                    return result
                }

                is Outcome.Success -> {
                    dogs += result.data
                    if (result.data.size < PAGE_SIZE) {
                        return Outcome.Success(dogs.map { it.toSourcedBreed() }.distinctBy { it.normalizedName })
                    }
                }
            }
        }
        return Outcome.Failure(DataError.Remote.Unknown)
    }

    private suspend fun fetchPage(offset: Int): Outcome<List<ApiNinjasDogDto>, DataError.Remote> =
        safeCall {
            httpClient.get("$BASE_URL/dogs") {
                header(API_KEY_HEADER, apiKey)
                parameter(FILTER_PARAMETER, FILTER_VALUE)
                parameter(OFFSET_PARAMETER, offset)
            }
        }

    private fun ApiNinjasDogDto.toSourcedBreed() =
        SourcedBreed(
            source = id,
            normalizedName = BreedNameNormalizer.normalize(name),
            displayName = name,
            weightKg = range(minWeightMale, minWeightFemale, maxWeightMale, maxWeightFemale)?.convert(::poundsToKilograms),
            heightCm = range(minHeightMale, minHeightFemale, maxHeightMale, maxHeightFemale)?.convert(::inchesToCentimetres),
            lifeSpanYears = range(minLifeExpectancy, maxLifeExpectancy),
            traits = traits(),
            imageUrls = listOfNotNull(imageLink?.takeIf { it.isNotBlank() }),
        )

    private fun range(vararg bounds: Double?): MeasureRange? {
        val values = bounds.filterNotNull()
        return if (values.size < 2) null else MeasureRange(min = values.min(), max = values.max())
    }

    private fun MeasureRange.convert(conversion: (Double) -> Double) = MeasureRange(min = conversion(min), max = conversion(max))

    private fun ApiNinjasDogDto.traits(): Map<BreedTrait, Int> =
        listOfNotNull(
            goodWithChildren?.let { BreedTrait.GoodWithChildren to it },
            goodWithOtherDogs?.let { BreedTrait.GoodWithOtherDogs to it },
            goodWithStrangers?.let { BreedTrait.GoodWithStrangers to it },
            shedding?.let { BreedTrait.Shedding to it },
            grooming?.let { BreedTrait.Grooming to it },
            drooling?.let { BreedTrait.Drooling to it },
            coatLength?.let { BreedTrait.CoatLength to it },
            playfulness?.let { BreedTrait.Playfulness to it },
            protectiveness?.let { BreedTrait.Protectiveness to it },
            trainability?.let { BreedTrait.Trainability to it },
            energy?.let { BreedTrait.Energy to it },
            barking?.let { BreedTrait.Barking to it },
        ).toMap()

    companion object {
        val ID = BreedSourceId("api-ninjas")
        const val BASE_URL = "https://api.api-ninjas.com/v1"
        const val API_KEY_HEADER = "X-Api-Key"
        const val PAGE_SIZE = 20
        const val MAX_PAGES = 50
        private const val FILTER_PARAMETER = "min_life_expectancy"
        private const val FILTER_VALUE = 1
        private const val OFFSET_PARAMETER = "offset"
    }
}
