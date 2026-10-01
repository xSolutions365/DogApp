package com.example.dogapp.testing

import com.example.dogapp.core.domain.draw.DailyDraw
import com.example.dogapp.core.domain.draw.DailyDrawRepository
import com.example.dogapp.core.domain.draw.DrawDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDailyDrawRepository(
    initial: List<DailyDraw> = emptyList(),
) : DailyDrawRepository {
    val draws = MutableStateFlow(initial.associateBy { it.day })

    override fun observeDraws(): Flow<List<DailyDraw>> = draws.map { all -> all.values.sortedByDescending { it.day.isoDate } }

    override fun observeDraw(day: DrawDay): Flow<DailyDraw?> = draws.map { it[day] }

    override suspend fun draw(day: DrawDay): DailyDraw? = draws.value[day]

    override suspend fun drawnBreedIds(): Set<String> = draws.value.values.mapTo(mutableSetOf()) { it.breedId }

    override suspend fun record(
        day: DrawDay,
        breedId: String,
        name: String,
    ): DailyDraw {
        draws.value[day]?.let { return it }
        val draw = DailyDraw(day = day, breedId = breedId, name = name, drawnAtEpochMillis = draws.value.size.toLong())
        draws.value += day to draw
        return draw
    }
}
