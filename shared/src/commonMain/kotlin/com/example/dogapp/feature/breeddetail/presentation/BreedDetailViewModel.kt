package com.example.dogapp.feature.breeddetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.example.dogapp.core.domain.breed.GetBreedGalleryUseCase
import com.example.dogapp.core.domain.breed.ObserveBreedUseCase
import com.example.dogapp.core.domain.onFailure
import com.example.dogapp.core.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreedDetailViewModel(
    private val breedId: String,
    observeBreed: ObserveBreedUseCase,
    private val getBreedGallery: GetBreedGalleryUseCase,
) : ViewModel() {
    private val logger = Logger.withTag("BreedDetail")
    private val breed = observeBreed(breedId)
    private val isFavourite = MutableStateFlow(false)
    private val gallery = MutableStateFlow<List<String>>(emptyList())

    val state: StateFlow<BreedDetailUiState> =
        combine(breed, isFavourite, gallery) { breed, isFavourite, gallery ->
            if (breed == null) {
                BreedDetailUiState.NotFound
            } else {
                BreedDetailUiState.Loaded(breed = breed, images = (breed.imageUrls + gallery).distinct(), isFavourite = isFavourite)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), BreedDetailUiState.Loading)

    init {
        viewModelScope.launch {
            getBreedGallery(breed.filterNotNull().first())
                .onSuccess { gallery.value = it }
                .onFailure { error -> logger.w { "Gallery for $breedId unavailable: $error" } }
        }
    }

    fun onAction(action: BreedDetailAction) {
        when (action) {
            BreedDetailAction.ToggleFavourite -> isFavourite.update { !it }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
