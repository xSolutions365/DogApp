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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ExploreViewModel(
    private val observeBreeds: ObserveBreedsUseCase,
    private val searchBreeds: SearchBreedsUseCase,
    private val breedSync: BreedSyncCoordinator,
    observeFavouriteIds: ObserveFavouriteIdsUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val state: StateFlow<ExploreUiState> =
        combine(observeBreeds(), query, breedSync.status, observeFavouriteIds()) { breeds, query, syncStatus, favouriteIds ->
            ExploreUiState(query = query, content = content(breeds, query, syncStatus, favouriteIds))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExploreUiState())

    init {
        breedSync.ensureLoaded()
    }

    fun onAction(action: ExploreAction) {
        when (action) {
            is ExploreAction.QueryChange -> query.value = action.query
            ExploreAction.Retry -> breedSync.retry()
        }
    }

    private fun content(
        breeds: List<Breed>,
        query: String,
        syncStatus: BreedSyncStatus,
        favouriteIds: Set<String>,
    ): ExploreContent {
        if (breeds.isEmpty()) {
            return when (syncStatus) {
                BreedSyncStatus.NotStarted, BreedSyncStatus.Syncing -> ExploreContent.Loading
                BreedSyncStatus.Done -> ExploreContent.Results(emptyList())
                is BreedSyncStatus.Failed -> ExploreContent.Error(syncStatus.error.toStringResource())
            }
        }
        val matches = searchBreeds(breeds, query)
        return if (matches.isEmpty()) {
            ExploreContent.NoMatches(query)
        } else {
            ExploreContent.Results(
                matches.map {
                    it.toListItem(
                        isFavourite =
                            it.id in favouriteIds,
                    )
                },
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
