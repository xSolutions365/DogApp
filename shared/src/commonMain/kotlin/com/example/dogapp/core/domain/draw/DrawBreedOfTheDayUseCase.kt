package com.example.dogapp.core.domain.draw

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedRepository
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class DrawBreedOfTheDayUseCase(
    private val draws: DailyDrawRepository,
    private val breeds: BreedRepository,
    private val today: TodayProvider,
    private val random: Random = Random.Default,
) {
    suspend operator fun invoke(): Outcome<DailyDraw, DrawError> {
        val day = today.today()
        draws.draw(day)?.let { return Outcome.Success(it) }
        val allBreeds = breeds.observeBreeds().first()
        val eligible = allBreeds.filter { it.imageUrls.isNotEmpty() }.ifEmpty { allBreeds }
        if (eligible.isEmpty()) return Outcome.Failure(DrawError.NoBreeds)
        val alreadyDrawn = draws.drawnBreedIds()
        val pool = eligible.filterNot { it.id in alreadyDrawn }.ifEmpty { eligible }
        val pick = pool.random(random)
        return Outcome.Success(draws.record(day = day, breedId = pick.id, name = pick.name))
    }
}
