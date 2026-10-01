package com.example.dogapp.core.data.breed

import com.example.dogapp.core.data.breed.local.SourcedBreedDao
import com.example.dogapp.core.data.breed.local.toEntity
import com.example.dogapp.core.data.breed.local.toSourcedBreed
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultBreedRepository(
    private val sources: List<BreedSource>,
    private val dao: SourcedBreedDao,
) : BreedRepository {
    private val consolidator = BreedConsolidator(precedence = sources.sortedBy { it.precedence }.map { it.id })

    override suspend fun syncBreeds(): BreedSyncResult {
        val outcomes = coroutineScope { sources.map { source -> async { source.id to source.fetchBreeds() } }.awaitAll() }
        dao.deleteSourcesNotIn(sources.map { it.id.value })
        outcomes.forEach { (id, outcome) ->
            if (outcome is Outcome.Success) dao.replaceSource(id.value, outcome.data.map { it.toEntity() })
        }
        return BreedSyncResult(
            succeededSources = outcomes.filter { (_, outcome) -> outcome is Outcome.Success }.map { (id, _) -> id }.toSet(),
            failures = outcomes.mapNotNull { (id, outcome) -> (outcome as? Outcome.Failure)?.let { id to it.error } }.toMap(),
        )
    }

    override fun observeBreeds(): Flow<List<Breed>> =
        dao.observeAll().map { entities -> consolidator.consolidate(entities.map { it.toSourcedBreed() }) }
}
