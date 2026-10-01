package com.example.dogapp.feature.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.breed.SearchBreedsUseCase
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import com.example.dogapp.core.domain.favourite.ObserveFavouriteIdsUseCase
import com.example.dogapp.core.presentation.error.toStringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val observeBreeds: ObserveBreedsUseCase,
    private val searchBreeds: SearchBreedsUseCase,
    private val syncBreeds: SyncBreedsUseCase,
    observeFavouriteIds: ObserveFavouriteIdsUseCase,
) : ViewModel() {
    private sealed interface SyncStatus {
        data object NotStarted : SyncStatus

        data object Syncing : SyncStatus

        data object Done : SyncStatus

        data class Failed(
            val error: DataError.Remote,
        ) : SyncStatus
    }

    private val query = MutableStateFlow("")
    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.NotStarted)

    val state: StateFlow<ExploreUiState> =
        combine(observeBreeds(), query, syncStatus, observeFavouriteIds()) { breeds, query, syncStatus, favouriteIds ->
            ExploreUiState(query = query, content = content(breeds, query, syncStatus, favouriteIds))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExploreUiState())

    init {
        viewModelScope.launch {
            if (observeBreeds().first().isEmpty()) sync()
        }
    }

    fun onAction(action: ExploreAction) {
        when (action) {
            is ExploreAction.QueryChange -> query.value = action.query
            ExploreAction.Retry -> viewModelScope.launch { sync() }
        }
    }

    private suspend fun sync() {
        if (syncStatus.value == SyncStatus.Syncing) return
        syncStatus.value = SyncStatus.Syncing
        syncStatus.value =
            when (val outcome = syncBreeds()) {
                is Outcome.Success -> SyncStatus.Done
                is Outcome.Failure -> SyncStatus.Failed(outcome.error)
            }
    }

    private fun content(
        breeds: List<Breed>,
        query: String,
        syncStatus: SyncStatus,
        favouriteIds: Set<String>,
    ): ExploreContent {
        if (breeds.isEmpty()) {
            return when (syncStatus) {
                SyncStatus.NotStarted, SyncStatus.Syncing -> ExploreContent.Loading
                SyncStatus.Done -> ExploreContent.Results(emptyList())
                is SyncStatus.Failed -> ExploreContent.Error(syncStatus.error.toStringResource())
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
