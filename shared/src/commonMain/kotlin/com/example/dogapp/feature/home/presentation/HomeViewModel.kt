package com.example.dogapp.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncStatus
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.draw.DrawBreedOfTheDayUseCase
import com.example.dogapp.core.domain.draw.ObserveTodaysDrawUseCase
import com.example.dogapp.core.domain.draw.ResolvedDraw
import com.example.dogapp.core.presentation.error.toStringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    observeBreeds: ObserveBreedsUseCase,
    observeTodaysDraw: ObserveTodaysDrawUseCase,
    private val drawBreedOfTheDay: DrawBreedOfTheDayUseCase,
    private val breedSync: BreedSyncCoordinator,
) : ViewModel() {
    private val isDrawing = MutableStateFlow(false)

    val state: StateFlow<HomeUiState> =
        combine(observeBreeds(), observeTodaysDraw(), breedSync.status, isDrawing) { breeds, todaysDraw, syncStatus, isDrawing ->
            HomeUiState(content = content(breeds, todaysDraw, syncStatus, isDrawing))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

    init {
        breedSync.ensureLoaded()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Reveal -> reveal()
            HomeAction.Retry -> breedSync.syncNow()
        }
    }

    private fun reveal() {
        if (isDrawing.value) return
        isDrawing.value = true
        viewModelScope.launch {
            drawBreedOfTheDay()
            isDrawing.value = false
        }
    }

    private fun content(
        breeds: List<Breed>,
        todaysDraw: ResolvedDraw?,
        syncStatus: BreedSyncStatus,
        isDrawing: Boolean,
    ): HomeContent {
        if (todaysDraw != null) return HomeContent.Revealed(todaysDraw.toDrawnBreed())
        if (breeds.isNotEmpty()) return HomeContent.NotRevealed(isDrawing = isDrawing)
        return when (syncStatus) {
            BreedSyncStatus.NotStarted, BreedSyncStatus.Syncing -> HomeContent.Loading
            is BreedSyncStatus.Done -> HomeContent.NoBreeds
            is BreedSyncStatus.Failed -> HomeContent.Error(syncStatus.error.toStringResource())
        }
    }

    private fun ResolvedDraw.toDrawnBreed(): DrawnBreed =
        breed?.let {
            DrawnBreed.Available(breedId = it.id, name = it.name, subtitle = it.breedGroup, imageUrls = it.imageUrls)
        } ?: DrawnBreed.Unavailable(breedId = draw.breedId, name = draw.name)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
