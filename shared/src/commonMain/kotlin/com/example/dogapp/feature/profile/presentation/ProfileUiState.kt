package com.example.dogapp.feature.profile.presentation

data class ProfileUiState(
    val favourites: List<FavouriteListItem>? = null,
)

sealed interface FavouriteListItem {
    val breedId: String
    val name: String

    data class Available(
        override val breedId: String,
        override val name: String,
        val subtitle: String?,
        val imageUrls: List<String>,
    ) : FavouriteListItem

    data class Unavailable(
        override val breedId: String,
        override val name: String,
    ) : FavouriteListItem
}
