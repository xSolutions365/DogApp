package com.example.dogapp.feature.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncStatus
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.breed.SearchBreedsUseCase
import com.example.dogapp.core.domain.favourite.ObserveFavouriteIdsUseCase
import com.example.dogapp.core.presentation.error.toStringResource
import com.example.dogapp.feature.explore.domain.AvailableFiltersUseCase
import com.example.dogapp.feature.explore.domain.BreedFilters
import com.example.dogapp.feature.explore.domain.FilterBreedsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val observeBreeds: ObserveBreedsUseCase,
    private val searchBreeds: SearchBreedsUseCase,
    private val filterBreeds: FilterBreedsUseCase,
    private val availableFilters: AvailableFiltersUseCase,
    private val breedSync: BreedSyncCoordinator,
    observeFavouriteIds: ObserveFavouriteIdsUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filters = MutableStateFlow(BreedFilters())

    val state: StateFlow<ExploreUiState> =
        combine(
            observeBreeds(),
            query,
            filters,
            breedSync.status,
            observeFavouriteIds(),
        ) { breeds, query, filters, syncStatus, favouriteIds ->
            ExploreUiState(
                query = query,
                filters = filters,
                availableFilters = availableFilters(breeds),
                groupOptions = breeds.mapNotNull { it.breedGroup }.distinct().sorted(),
                content = content(breeds, query, filters, syncStatus, favouriteIds),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExploreUiState())

    init {
        breedSync.ensureLoaded()
        viewModelScope.launch {
            breedSync.status.collect { status ->
                if (status == BreedSyncStatus.Syncing) filters.value = BreedFilters()
            }
        }
    }

    fun onAction(action: ExploreAction) {
        when (action) {
            is ExploreAction.QueryChange -> query.value = action.query
            is ExploreAction.FiltersChange -> filters.value = action.filters
            ExploreAction.Retry -> breedSync.retry()
        }
    }

    private fun content(
        breeds: List<Breed>,
        query: String,
        filters: BreedFilters,
        syncStatus: BreedSyncStatus,
        favouriteIds: Set<String>,
    ): ExploreContent {
        if (syncStatus == BreedSyncStatus.Syncing) return ExploreContent.Loading
        if (breeds.isEmpty()) {
            return when (syncStatus) {
                BreedSyncStatus.NotStarted, BreedSyncStatus.Syncing -> ExploreContent.Loading
                BreedSyncStatus.Done -> ExploreContent.Results(emptyList())
                is BreedSyncStatus.Failed -> ExploreContent.Error(syncStatus.error.toStringResource())
            }
        }
        val filtered = filterBreeds(searchBreeds(breeds, query), filters, favouriteIds)
        return if (filtered.breeds.isEmpty()) {
            ExploreContent.NoMatches(query, hasFilters = filters.isActive, hiddenForMissingInfo = filtered.hiddenForMissingInfo)
        } else {
            ExploreContent.Results(
                filtered.breeds.map { it.toListItem(isFavourite = it.id in favouriteIds) },
                hiddenForMissingInfo = filtered.hiddenForMissingInfo,
            )
        }
    }

    private fun Breed.toListItem(isFavourite: Boolean) =
        BreedListItem(
            id = id,
            name = name,
            subtitle = breedGroup ?: temperament.take(SUBTITLE_TEMPERAMENT_COUNT).joinToString(", ").ifEmpty { null },
            imageUrls = imageUrls,
            isFavourite = isFavourite,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val SUBTITLE_TEMPERAMENT_COUNT = 2
    }
}
