package com.example.dogapp.core.data.draw

import androidx.room.Room
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.database.buildDogAppDatabase
import com.example.dogapp.core.domain.draw.DailyDraw
import com.example.dogapp.core.domain.draw.DrawDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultDailyDrawRepositoryTest {
    private val database: DogAppDatabase = buildDogAppDatabase(Room.inMemoryDatabaseBuilder<DogAppDatabase>())

    private class SteppingClock : Clock {
        private var millis = 1_000L

        override fun now(): Instant = Instant.fromEpochMilliseconds(millis++)
    }

    private val repository = DefaultDailyDrawRepository(dao = database.dailyDrawDao(), clock = SteppingClock())

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun eachDayKeepsOnlyItsFirstDrawAndHistoryIsNewestFirst() =
        runBlocking {
            val first = repository.record(DrawDay("2026-09-30"), "akita", "Akita")
            val second = repository.record(DrawDay("2026-09-30"), "beagle", "Beagle")
            repository.record(DrawDay("2026-10-01"), "beagle", "Beagle")

            assertEquals(DailyDraw(DrawDay("2026-09-30"), "akita", "Akita", 1_000), first)
            assertEquals(first, second)
            assertEquals(first, repository.draw(DrawDay("2026-09-30")))
            assertEquals(first, repository.observeDraw(DrawDay("2026-09-30")).first())
            assertEquals(null, repository.observeDraw(DrawDay("2026-10-02")).first())
            assertEquals(listOf("2026-10-01", "2026-09-30"), repository.observeDraws().first().map { it.day.isoDate })
            assertEquals(setOf("akita", "beagle"), repository.drawnBreedIds())
        }
}
