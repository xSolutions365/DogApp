package com.example.dogapp.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.favourite.FavouriteBreed
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.RemoveFavouriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    observeFavouriteBreeds: ObserveFavouriteBreedsUseCase,
    private val removeFavourite: RemoveFavouriteUseCase,
) : ViewModel() {
    val state: StateFlow<ProfileUiState> =
        observeFavouriteBreeds()
            .map { favourites -> ProfileUiState(favourites = favourites.map { it.toListItem() }) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProfileUiState())

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.RemoveFavourite -> viewModelScope.launch { removeFavourite(action.breedId) }
        }
    }

    private fun FavouriteBreed.toListItem(): FavouriteListItem =
        when (this) {
            is FavouriteBreed.Available -> {
                FavouriteListItem.Available(
                    breedId = breed.id,
                    name = breed.name,
                    subtitle = breed.breedGroup,
                    imageUrl = breed.imageUrls.firstOrNull(),
                )
            }

            is FavouriteBreed.Unavailable -> {
                FavouriteListItem.Unavailable(breedId = breedId, name = name)
            }
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
