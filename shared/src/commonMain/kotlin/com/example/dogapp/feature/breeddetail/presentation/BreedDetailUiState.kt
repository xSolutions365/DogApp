package com.example.dogapp.feature.breeddetail.presentation

import com.example.dogapp.core.domain.breed.Breed

sealed interface BreedDetailUiState {
    data object Loading : BreedDetailUiState

    data object NotFound : BreedDetailUiState

    data class Loaded(
        val breed: Breed,
        val images: List<String>,
        val isFavourite: Boolean,
    ) : BreedDetailUiState
}
