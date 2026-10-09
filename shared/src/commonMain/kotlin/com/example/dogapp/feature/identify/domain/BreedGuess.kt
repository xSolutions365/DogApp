package com.example.dogapp.feature.identify.domain

data class BreedGuess(
    val breedId: String,
    val name: String,
    val confidence: Float,
)
