package com.example.dogapp.feature.home.presentation

import org.jetbrains.compose.resources.StringResource

data class HomeUiState(
    val content: HomeContent = HomeContent.Pending,
)

sealed interface HomeContent {
    data object Pending : HomeContent

    data object Loading : HomeContent

    data class Error(
        val message: StringResource,
    ) : HomeContent

    data object NoBreeds : HomeContent

    data class NotRevealed(
        val isDrawing: Boolean,
    ) : HomeContent

    data class Revealed(
        val breed: DrawnBreed,
    ) : HomeContent
}

sealed interface DrawnBreed {
    val breedId: String
    val name: String

    data class Available(
        override val breedId: String,
        override val name: String,
        val subtitle: String?,
        val imageUrls: List<String>,
    ) : DrawnBreed

    data class Unavailable(
        override val breedId: String,
        override val name: String,
    ) : DrawnBreed
}
