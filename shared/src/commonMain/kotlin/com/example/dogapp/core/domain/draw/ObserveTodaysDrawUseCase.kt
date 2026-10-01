package com.example.dogapp.core.domain.draw

import com.example.dogapp.core.domain.breed.BreedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveTodaysDrawUseCase(
    private val draws: DailyDrawRepository,
    private val breeds: BreedRepository,
    private val today: TodayProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<ResolvedDraw?> =
        today.observeToday().flatMapLatest { day ->
            combine(draws.observeDraw(day), breeds.observeBreeds()) { draw, breeds ->
                draw?.let { ResolvedDraw(draw = it, breed = breeds.firstOrNull { breed -> breed.id == it.breedId }) }
            }
        }
}
