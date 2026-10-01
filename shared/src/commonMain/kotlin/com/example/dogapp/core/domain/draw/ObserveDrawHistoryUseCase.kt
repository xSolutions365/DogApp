package com.example.dogapp.core.domain.draw

import com.example.dogapp.core.domain.breed.BreedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveDrawHistoryUseCase(
    private val draws: DailyDrawRepository,
    private val breeds: BreedRepository,
) {
    operator fun invoke(): Flow<List<ResolvedDraw>> =
        combine(draws.observeDraws(), breeds.observeBreeds()) { draws, breeds ->
            val breedsById = breeds.associateBy { it.id }
            draws.map { ResolvedDraw(draw = it, breed = breedsById[it.breedId]) }
        }
}
