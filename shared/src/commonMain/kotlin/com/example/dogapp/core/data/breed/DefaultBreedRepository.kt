package com.example.dogapp.core.data.breed

import com.example.dogapp.core.data.breed.local.SourcedBreedDao
import com.example.dogapp.core.data.breed.local.toEntity
import com.example.dogapp.core.data.breed.local.toSourcedBreed
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class DefaultBreedRepository(
    private val sources: List<BreedSource>,
    private val imageSources: List<BreedImageSource>,
    private val dao: SourcedBreedDao,
) : BreedRepository {
    private val consolidator =
        BreedConsolidator(
            precedence = sources.sortedBy { it.precedence }.map { it.id },
            sourceNames = sources.associate { it.id to it.displayName },
        )

    override suspend fun syncBreeds(): BreedSyncResult {
        val outcomes = coroutineScope { sources.map { source -> async { source.id to source.fetchBreeds() } }.awaitAll() }
        dao.deleteSourcesNotIn(sources.map { it.id.value })
        outcomes.forEach { (id, outcome) ->
            if (outcome is Outcome.Success) dao.replaceSource(id.value, outcome.data.map { it.toEntity() })
        }
        backfillMissingThumbnails()
        return BreedSyncResult(
            succeededSources = outcomes.filter { (_, outcome) -> outcome is Outcome.Success }.map { (id, _) -> id }.toSet(),
            failures = outcomes.mapNotNull { (id, outcome) -> (outcome as? Outcome.Failure)?.let { id to it.error } }.toMap(),
        )
    }

    override fun observeBreeds(): Flow<List<Breed>> =
        dao.observeAll().map { entities -> consolidator.consolidate(entities.map { it.toSourcedBreed() }) }

    override suspend fun fetchGallery(breed: Breed): Outcome<List<String>, DataError.Remote> {
        val applicable = imageSources.mapNotNull { source -> breed.sourceReferences[source.id]?.let { source to it } }
        if (applicable.isEmpty()) return Outcome.Success(emptyList())
        val outcomes =
            coroutineScope {
                applicable
                    .sortedBy { (source, _) -> breed.sources.indexOf(source.id) }
                    .map { (source, reference) -> async { source.fetchImages(reference) } }
                    .awaitAll()
            }
        val images = outcomes.flatMap { (it as? Outcome.Success)?.data.orEmpty() }
        val firstFailure = outcomes.firstNotNullOfOrNull { (it as? Outcome.Failure)?.error }
        return if (outcomes.none { it is Outcome.Success } &&
            firstFailure != null
        ) {
            Outcome.Failure(firstFailure)
        } else {
            Outcome.Success(images.distinct())
        }
    }

    private suspend fun backfillMissingThumbnails() {
        val breedsWithoutImages = consolidator.consolidate(dao.getAll().map { it.toSourcedBreed() }).filter { it.imageUrls.isEmpty() }
        val permits = Semaphore(THUMBNAIL_BACKFILL_CONCURRENCY)
        coroutineScope {
            breedsWithoutImages
                .map { breed -> async { permits.withPermit { backfillThumbnail(breed) } } }
                .awaitAll()
        }
    }

    private suspend fun backfillThumbnail(breed: Breed) {
        val candidates =
            imageSources
                .mapNotNull { source -> breed.sourceReferences[source.id]?.let { source to it } }
                .sortedBy { (source, _) -> breed.sources.indexOf(source.id) }
        for ((source, reference) in candidates) {
            val thumbnail = (source.fetchImages(reference) as? Outcome.Success)?.data?.firstOrNull() ?: continue
            dao.get(source.id.value, breed.id)?.let { entity -> dao.update(entity.copy(imageUrls = listOf(thumbnail))) }
            return
        }
    }

    private companion object {
        const val THUMBNAIL_BACKFILL_CONCURRENCY = 8
    }
}
