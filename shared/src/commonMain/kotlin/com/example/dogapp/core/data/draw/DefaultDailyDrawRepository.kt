package com.example.dogapp.core.data.draw

import com.example.dogapp.core.data.draw.local.DailyDrawDao
import com.example.dogapp.core.data.draw.local.DailyDrawEntity
import com.example.dogapp.core.domain.draw.DailyDraw
import com.example.dogapp.core.domain.draw.DailyDrawRepository
import com.example.dogapp.core.domain.draw.DrawDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class DefaultDailyDrawRepository(
    private val dao: DailyDrawDao,
    private val clock: Clock = Clock.System,
) : DailyDrawRepository {
    override fun observeDraws(): Flow<List<DailyDraw>> = dao.observeAll().map { entities -> entities.map { it.toDailyDraw() } }

    override fun observeDraw(day: DrawDay): Flow<DailyDraw?> = dao.observe(day.isoDate).map { it?.toDailyDraw() }

    override suspend fun draw(day: DrawDay): DailyDraw? = dao.get(day.isoDate)?.toDailyDraw()

    override suspend fun drawnBreedIds(): Set<String> = dao.drawnBreedIds().toSet()

    override suspend fun record(
        day: DrawDay,
        breedId: String,
        name: String,
    ): DailyDraw =
        dao
            .insertOrGetExisting(
                DailyDrawEntity(day = day.isoDate, breedId = breedId, name = name, drawnAtEpochMillis = clock.now().toEpochMilliseconds()),
            ).toDailyDraw()

    private fun DailyDrawEntity.toDailyDraw() = DailyDraw(DrawDay(day), breedId, name, drawnAtEpochMillis)
}
