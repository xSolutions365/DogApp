package com.example.dogapp.feature.identify.domain

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.map
import kotlinx.coroutines.flow.first

class IdentifyBreedUseCase(
    private val classifier: BreedClassifier,
    private val repository: BreedRepository,
) {
    suspend operator fun invoke(photo: ByteArray): Outcome<BreedIdentification, IdentifyError> =
        classifier.classify(photo).map { guesses ->
            identify(guesses, repository.observeBreeds().first().associateBy(Breed::id))
        }

    private fun identify(
        guesses: List<BreedGuess>,
        breedsById: Map<String, Breed>,
    ): BreedIdentification {
        if (guesses.sumOf { it.confidence.toDouble() } < MIN_DOG_CONFIDENCE) return BreedIdentification.NoDog
        val ranked =
            guesses
                .sortedByDescending(BreedGuess::confidence)
                .map { guess ->
                    val breed = breedsById[guess.breedId]
                    IdentifiedBreed(name = breed?.name ?: guess.name, confidence = guess.confidence, breed = breed)
                }
        val best = ranked.first()
        return if (best.confidence >= CONFIDENT_MATCH) {
            BreedIdentification.Confident(best)
        } else {
            BreedIdentification.Uncertain(ranked.take(CANDIDATE_COUNT))
        }
    }

    private companion object {
        const val MIN_DOG_CONFIDENCE = 0.3
        const val CONFIDENT_MATCH = 0.6f
        const val CANDIDATE_COUNT = 3
    }
}
