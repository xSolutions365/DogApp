package com.example.dogapp.core.domain.draw

import kotlinx.coroutines.flow.Flow

interface DailyDrawRepository {
    fun observeDraws(): Flow<List<DailyDraw>>

    fun observeDraw(day: DrawDay): Flow<DailyDraw?>

    suspend fun draw(day: DrawDay): DailyDraw?

    suspend fun drawnBreedIds(): Set<String>

    suspend fun record(
        day: DrawDay,
        breedId: String,
        name: String,
    ): DailyDraw
}
