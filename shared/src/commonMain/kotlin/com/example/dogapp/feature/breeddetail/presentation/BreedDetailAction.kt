package com.example.dogapp.feature.breeddetail.presentation

sealed interface BreedDetailAction {
    data object ToggleFavourite : BreedDetailAction
}
