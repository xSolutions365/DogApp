package com.example.dogapp.feature.identify.domain

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.testing.FakeBreedClassifier
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IdentifyBreedUseCaseTest {
    private val akita = testBreed("akita")
    private val beagle = testBreed("beagle")
    private val pug = testBreed("pug")
    private val classifier = FakeBreedClassifier()
    private val useCase = IdentifyBreedUseCase(classifier, FakeBreedRepository(listOf(akita, beagle, pug)))

    private fun guess(
        breedId: String,
        confidence: Float,
    ) = BreedGuess(breedId = breedId, name = breedId.replaceFirstChar(Char::uppercaseChar), confidence = confidence)

    @Test
    fun aClearWinnerIsTheOnlyMatch() =
        runTest {
            classifier.returns(Outcome.Success(listOf(guess("beagle", 0.1f), guess("akita", 0.8f))))

            assertEquals(
                Outcome.Success(BreedIdentification.Confident(IdentifiedBreed("Akita", 0.8f, akita))),
                useCase(byteArrayOf(1)),
            )
        }

    @Test
    fun withoutAClearWinnerTheTopThreeCandidatesAreOfferedInOrder() =
        runTest {
            classifier.returns(
                Outcome.Success(
                    listOf(guess("pug", 0.05f), guess("akita", 0.3f), guess("beagle", 0.4f), guess("eskimo dog", 0.1f)),
                ),
            )

            assertEquals(
                Outcome.Success(
                    BreedIdentification.Uncertain(
                        listOf(
                            IdentifiedBreed("Beagle", 0.4f, beagle),
                            IdentifiedBreed("Akita", 0.3f, akita),
                            IdentifiedBreed("Eskimo dog", 0.1f, null),
                        ),
                    ),
                ),
                useCase(byteArrayOf(1)),
            )
        }

    @Test
    fun aPhotoWithLittleDogInItFindsNoDog() =
        runTest {
            classifier.returns(Outcome.Success(listOf(guess("akita", 0.15f), guess("beagle", 0.1f))))
            assertEquals(Outcome.Success(BreedIdentification.NoDog), useCase(byteArrayOf(1)))

            classifier.returns(Outcome.Success(emptyList()))
            assertEquals(Outcome.Success(BreedIdentification.NoDog), useCase(byteArrayOf(1)))
        }

    @Test
    fun classifierFailuresPassThrough() =
        runTest {
            classifier.returns(Outcome.Failure(IdentifyError.ModelUnavailable))

            assertEquals(Outcome.Failure(IdentifyError.ModelUnavailable), useCase(byteArrayOf(1)))
        }
}
