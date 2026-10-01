package com.example.dogapp.core.data.breed

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedFetchResult
import com.example.dogapp.core.domain.breed.BreedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DefaultBreedRepository(
    private val sources: List<BreedSource>,
) : BreedRepository {
    override suspend fun fetchBreeds(): BreedFetchResult =
        coroutineScope {
            val outcomes = sources.map { source -> async { source.id to source.fetchBreeds() } }.awaitAll()
            BreedFetchResult(
                breeds = outcomes.flatMap { (_, outcome) -> (outcome as? Outcome.Success)?.data.orEmpty() },
                succeededSources = outcomes.filter { (_, outcome) -> outcome is Outcome.Success }.map { (id, _) -> id }.toSet(),
                failures = outcomes.mapNotNull { (id, outcome) -> (outcome as? Outcome.Failure)?.let { id to it.error } }.toMap(),
            )
        }
}
