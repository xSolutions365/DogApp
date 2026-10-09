package com.example.dogapp.feature.identify.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedNameNormalizer
import com.example.dogapp.feature.identify.domain.BreedGuess
import com.example.dogapp.feature.identify.domain.IdentifyError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageNetBreedClassifierTest {
    private fun classifier(outcome: Outcome<Map<String, Float>, IdentifyError>) =
        ImageNetBreedClassifier(
            object : ImageLabeler {
                override suspend fun label(photo: ByteArray) = outcome
            },
        )

    @Test
    fun keepsOnlyDogBreedLabelsAndMapsThemToBreedIds() =
        runTest {
            val scores =
                mapOf(
                    "background" to 0.01f,
                    "tabby" to 0.2f,
                    "golden retriever" to 0.5f,
                    "Boston bull" to 0.1f,
                    "Eskimo dog" to 0.05f,
                )

            val guesses = (classifier(Outcome.Success(scores)).classify(byteArrayOf(1)) as Outcome.Success).data

            assertEquals(
                setOf(
                    BreedGuess(breedId = "golden retriever", name = "Golden retriever", confidence = 0.5f),
                    BreedGuess(breedId = "boston terrier", name = "Boston bull", confidence = 0.1f),
                    BreedGuess(breedId = "eskimo dog", name = "Eskimo dog", confidence = 0.05f),
                ),
                guesses.toSet(),
            )
        }

    @Test
    fun readsLabelsThatListSynonymsAfterACommaByTheirFirstName() =
        runTest {
            val scores = mapOf("Maltese dog, Maltese terrier, Maltese" to 0.7f, "tabby, tabby cat" to 0.3f)

            val guesses = (classifier(Outcome.Success(scores)).classify(byteArrayOf(1)) as Outcome.Success).data

            assertEquals(listOf(BreedGuess(breedId = "maltese", name = "Maltese dog", confidence = 0.7f)), guesses)
        }

    @Test
    fun everyAliasBelongsToADogLabel() {
        val normalizedLabels = imageNetDogLabels.map(BreedNameNormalizer::normalize).toSet()

        assertEquals(emptySet(), imageNetBreedAliases.keys - normalizedLabels)
    }

    @Test
    fun labelingFailuresPassThrough() =
        runTest {
            assertEquals(
                Outcome.Failure(IdentifyError.UnreadablePhoto),
                classifier(Outcome.Failure(IdentifyError.UnreadablePhoto)).classify(byteArrayOf(1)),
            )
        }
}
