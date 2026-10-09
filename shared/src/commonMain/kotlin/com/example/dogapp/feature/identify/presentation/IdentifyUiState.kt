package com.example.dogapp.feature.identify.presentation

import org.jetbrains.compose.resources.StringResource

class IdentifyUiState(
    val photo: ByteArray? = null,
    val content: IdentifyContent = IdentifyContent.Empty,
)

sealed interface IdentifyContent {
    data object Empty : IdentifyContent

    data object Identifying : IdentifyContent

    data object NoDog : IdentifyContent

    data class Error(
        val message: StringResource,
    ) : IdentifyContent

    data class Matches(
        val isConfident: Boolean,
        val matches: List<BreedMatch>,
    ) : IdentifyContent
}

data class BreedMatch(
    val breedId: String?,
    val name: String,
    val confidencePercent: Int,
    val imageUrls: List<String>,
)
