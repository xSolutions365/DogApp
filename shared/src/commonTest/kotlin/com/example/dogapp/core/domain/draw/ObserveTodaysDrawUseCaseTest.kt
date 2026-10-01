package com.example.dogapp.core.domain.draw

import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.FakeDailyDrawRepository
import com.example.dogapp.testing.FakeTodayProvider
import com.example.dogapp.testing.testBreed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTodaysDrawUseCaseTest {
    private val yesterday = DailyDraw(DrawDay("2026-09-30"), "akita", "Akita", drawnAtEpochMillis = 1)
    private val today = DailyDraw(DrawDay("2026-10-01"), "blenheim spaniel", "Blenheim Spaniel", drawnAtEpochMillis = 2)
    private val draws = FakeDailyDrawRepository(listOf(yesterday, today))
    private val breeds = FakeBreedRepository(listOf(testBreed("akita")))
    private val todayProvider = FakeTodayProvider("2026-09-30")

    @Test
    fun todaysDrawFollowsTheDayAndResolvesToTheStoredBreedWhenItStillExists() =
        runTest {
            val observeTodaysDraw = ObserveTodaysDrawUseCase(draws, breeds, todayProvider)

            assertEquals(ResolvedDraw(yesterday, testBreed("akita")), observeTodaysDraw().first())

            todayProvider.day.value = DrawDay("2026-10-01")
            assertEquals(ResolvedDraw(today, breed = null), observeTodaysDraw().first())

            todayProvider.day.value = DrawDay("2026-10-02")
            assertEquals(null, observeTodaysDraw().first())
        }

    @Test
    fun historyListsEveryDrawNewestFirstWithItsBreedWhenItStillExists() =
        runTest {
            assertEquals(
                listOf(ResolvedDraw(today, breed = null), ResolvedDraw(yesterday, testBreed("akita"))),
                ObserveDrawHistoryUseCase(draws, breeds)().first(),
            )
        }
}
