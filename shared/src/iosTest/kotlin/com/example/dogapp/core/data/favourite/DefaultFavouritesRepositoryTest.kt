package com.example.dogapp.core.data.favourite

import androidx.room.Room
import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.breed.DefaultBreedRepository
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.database.buildDogAppDatabase
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.SourcedBreed
import com.example.dogapp.core.domain.favourite.FavouriteRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultFavouritesRepositoryTest {
    private val database: DogAppDatabase = buildDogAppDatabase(Room.inMemoryDatabaseBuilder<DogAppDatabase>())

    private class SteppingClock : Clock {
        private var millis = 1_000L

        override fun now(): Instant = Instant.fromEpochMilliseconds(millis++)
    }

    private val repository = DefaultFavouritesRepository(dao = database.favouriteBreedDao(), clock = SteppingClock())

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun toggleAddsThenRemovesAndFavouritesAreObservedNewestFirst() =
        runBlocking {
            repository.toggle("akita", "Akita")
            repository.toggle("beagle", "Beagle")

            assertEquals(
                listOf(FavouriteRecord("beagle", "Beagle", 1_001), FavouriteRecord("akita", "Akita", 1_000)),
                repository.observeFavourites().first(),
            )
            assertEquals(true, repository.observeIsFavourite("akita").first())

            repository.toggle("akita", "Akita")
            repository.remove("beagle")

            assertEquals(emptyList(), repository.observeFavourites().first())
            assertEquals(false, repository.observeIsFavourite("akita").first())
        }

    @Test
    fun favouritesSurviveBreedResyncsThatReplaceOrDropTheBreedRows() =
        runBlocking {
            val source = BreedSourceId("source")
            var breeds = listOf(SourcedBreed(source, "akita", "Akita"))
            val breedSource =
                object : BreedSource {
                    override val id = source
                    override val displayName = "Source"
                    override val precedence = 0

                    override suspend fun fetchBreeds() = Outcome.Success(breeds)
                }
            val breedRepository =
                DefaultBreedRepository(sources = listOf(breedSource), imageSources = emptyList(), dao = database.sourcedBreedDao())
            breedRepository.syncBreeds()
            repository.toggle("akita", "Akita")

            breeds = emptyList()
            breedRepository.syncBreeds()

            assertEquals(emptyList(), breedRepository.observeBreeds().first())
            assertEquals(listOf("akita"), repository.observeFavourites().first().map { it.breedId })
        }
}
