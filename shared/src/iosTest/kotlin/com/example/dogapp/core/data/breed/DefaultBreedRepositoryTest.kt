package com.example.dogapp.core.data.breed

import androidx.room.Room
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.database.buildDogAppDatabase
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.domain.breed.SourcedBreed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultBreedRepositoryTest {
    private val database: DogAppDatabase = buildDogAppDatabase(Room.inMemoryDatabaseBuilder<DogAppDatabase>())
    private val primary = BreedSourceId("primary")
    private val secondary = BreedSourceId("secondary")

    private class FakeSource(
        override val id: BreedSourceId,
        override val precedence: Int,
        override val displayName: String = id.value,
        var outcome: suspend () -> Outcome<List<SourcedBreed>, DataError.Remote>,
    ) : BreedSource {
        override suspend fun fetchBreeds() = outcome()
    }

    private fun repository(vararg sources: BreedSource) =
        DefaultBreedRepository(sources = sources.toList(), imageSources = emptyList(), dao = database.sourcedBreedDao())

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncStoresEverySucceededSourceAndObservedBreedsAreConsolidated() =
        runBlocking {
            val repository =
                repository(
                    FakeSource(primary, precedence = 0) {
                        Outcome.Success(
                            listOf(SourcedBreed(primary, "border collie", "Border Collie", weightKg = MeasureRange(14.0, 25.0))),
                        )
                    },
                    FakeSource(secondary, precedence = 1) {
                        Outcome.Success(
                            listOf(
                                SourcedBreed(secondary, "border collie", "Border Collie (secondary)", heightCm = MeasureRange(48.0, 56.0)),
                                SourcedBreed(secondary, "beagle", "Beagle"),
                            ),
                        )
                    },
                )

            val result = repository.syncBreeds()
            val breeds = repository.observeBreeds().first()

            assertEquals(setOf(primary, secondary), result.succeededSources)
            assertEquals(emptyMap(), result.failures)
            assertEquals(listOf("Beagle", "Border Collie"), breeds.map { it.name })
            val borderCollie = breeds.single { it.id == "border collie" }
            assertEquals(MeasureRange(14.0, 25.0), borderCollie.weightKg)
            assertEquals(MeasureRange(48.0, 56.0), borderCollie.heightCm)
            assertEquals(listOf(primary, secondary), borderCollie.sources)
        }

    @Test
    fun failedSourceKeepsItsPreviousRowsWhileSucceededSourcesAreReplaced() =
        runBlocking {
            val primarySource = FakeSource(primary, precedence = 0) { Outcome.Success(listOf(SourcedBreed(primary, "akita", "Akita"))) }
            val secondarySource =
                FakeSource(secondary, precedence = 1) { Outcome.Success(listOf(SourcedBreed(secondary, "beagle", "Beagle"))) }
            val repository = repository(primarySource, secondarySource)
            repository.syncBreeds()

            primarySource.outcome = { Outcome.Success(listOf(SourcedBreed(primary, "boxer", "Boxer"))) }
            secondarySource.outcome = { Outcome.Failure(DataError.Remote.NoConnection) }
            val result = repository.syncBreeds()

            assertEquals(mapOf(secondary to DataError.Remote.NoConnection), result.failures)
            assertEquals(listOf("Beagle", "Boxer"), repository.observeBreeds().first().map { it.name })
        }

    @Test
    fun rowsOfSourcesThatAreNoLongerRegisteredAreRemovedOnSync() =
        runBlocking {
            repository(
                FakeSource(primary, precedence = 0) { Outcome.Success(listOf(SourcedBreed(primary, "akita", "Akita"))) },
                FakeSource(secondary, precedence = 1) { Outcome.Success(listOf(SourcedBreed(secondary, "beagle", "Beagle"))) },
            ).syncBreeds()

            val withoutSecondary =
                repository(FakeSource(primary, precedence = 0) { Outcome.Success(listOf(SourcedBreed(primary, "akita", "Akita"))) })
            withoutSecondary.syncBreeds()

            assertEquals(listOf("Akita"), withoutSecondary.observeBreeds().first().map { it.name })
        }

    @Test
    fun mergePrecedenceFollowsEachRegisteredSourcesDeclaredPrecedence() =
        runBlocking {
            val repository =
                repository(
                    FakeSource(secondary, precedence = 1) {
                        Outcome.Success(listOf(SourcedBreed(secondary, "akita", "Akita (secondary)", origin = "Secondary")))
                    },
                    FakeSource(
                        primary,
                        precedence = 0,
                    ) { Outcome.Success(listOf(SourcedBreed(primary, "akita", "Akita", origin = "Japan"))) },
                )
            repository.syncBreeds()

            val akita = repository.observeBreeds().first().single()

            assertEquals("Akita", akita.name)
            assertEquals("Japan", akita.origin)
            assertEquals(listOf(primary, secondary), akita.sources)
        }

    @Test
    fun sourcesAreFetchedConcurrently() =
        runBlocking {
            val firstStarted = CompletableDeferred<Unit>()
            val secondStarted = CompletableDeferred<Unit>()
            val repository =
                repository(
                    FakeSource(primary, precedence = 0) {
                        firstStarted.complete(Unit)
                        secondStarted.await()
                        Outcome.Success(emptyList())
                    },
                    FakeSource(secondary, precedence = 1) {
                        secondStarted.complete(Unit)
                        firstStarted.await()
                        Outcome.Success(emptyList())
                    },
                )

            assertEquals(setOf(primary, secondary), repository.syncBreeds().succeededSources)
        }

    private class FakeImageSource(
        override val id: BreedSourceId,
        val outcome: (String) -> Outcome<List<String>, DataError.Remote>,
    ) : BreedImageSource {
        val requestedReferences = mutableListOf<String>()

        override suspend fun fetchImages(sourceReference: String): Outcome<List<String>, DataError.Remote> {
            requestedReferences += sourceReference
            return outcome(sourceReference)
        }
    }

    @Test
    fun galleryCombinesImagesFromEveryImageSourceThatKnowsTheBreed() =
        runBlocking {
            val primaryImages =
                FakeImageSource(primary) { reference -> Outcome.Success(listOf("https://p/$reference/1.jpg", "https://shared.jpg")) }
            val secondaryImages = FakeImageSource(secondary) { Outcome.Failure(DataError.Remote.NoConnection) }
            val unrelatedImages = FakeImageSource(BreedSourceId("unrelated")) { Outcome.Success(listOf("https://unrelated.jpg")) }
            val repository =
                DefaultBreedRepository(
                    sources =
                        listOf(
                            FakeSource(primary, precedence = 0) {
                                Outcome.Success(
                                    listOf(
                                        SourcedBreed(
                                            primary,
                                            "akita",
                                            "Akita",
                                            sourceReference = "1",
                                            imageUrls = listOf("https://shared.jpg"),
                                        ),
                                    ),
                                )
                            },
                            FakeSource(secondary, precedence = 1) {
                                Outcome.Success(listOf(SourcedBreed(secondary, "akita", "Akita", sourceReference = "akita")))
                            },
                        ),
                    imageSources = listOf(secondaryImages, primaryImages, unrelatedImages),
                    dao = database.sourcedBreedDao(),
                )
            repository.syncBreeds()
            val akita = repository.observeBreeds().first().single()

            assertEquals(Outcome.Success(listOf("https://p/1/1.jpg", "https://shared.jpg")), repository.fetchGallery(akita))
            assertEquals(listOf("1"), primaryImages.requestedReferences)
            assertEquals(listOf("akita"), secondaryImages.requestedReferences)
            assertEquals(emptyList(), unrelatedImages.requestedReferences)
        }

    @Test
    fun galleryFailsOnlyWhenEveryApplicableImageSourceFailsAndIsEmptyWithoutReferences() =
        runBlocking {
            val failing = FakeImageSource(primary) { Outcome.Failure(DataError.Remote.Server(503)) }
            val repository =
                DefaultBreedRepository(
                    sources =
                        listOf(
                            FakeSource(primary, precedence = 0) {
                                Outcome.Success(
                                    listOf(
                                        SourcedBreed(primary, "akita", "Akita", sourceReference = "1"),
                                        SourcedBreed(primary, "boxer", "Boxer"),
                                    ),
                                )
                            },
                        ),
                    imageSources = listOf(failing),
                    dao = database.sourcedBreedDao(),
                )
            repository.syncBreeds()
            val breeds = repository.observeBreeds().first()

            assertEquals(Outcome.Failure(DataError.Remote.Server(503)), repository.fetchGallery(breeds.single { it.id == "akita" }))
            assertEquals(Outcome.Success(emptyList()), repository.fetchGallery(breeds.single { it.id == "boxer" }))
        }
}
