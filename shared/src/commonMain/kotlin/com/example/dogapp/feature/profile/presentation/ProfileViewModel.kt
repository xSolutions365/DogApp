package com.example.dogapp.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.draw.ObserveDrawHistoryUseCase
import com.example.dogapp.core.domain.draw.ResolvedDraw
import com.example.dogapp.core.domain.draw.TodayProvider
import com.example.dogapp.core.domain.favourite.FavouriteBreed
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.RemoveFavouriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    observeFavouriteBreeds: ObserveFavouriteBreedsUseCase,
    observeDrawHistory: ObserveDrawHistoryUseCase,
    today: TodayProvider,
    private val removeFavourite: RemoveFavouriteUseCase,
) : ViewModel() {
    val state: StateFlow<ProfileUiState> =
        combine(observeFavouriteBreeds(), observeDrawHistory(), today.observeToday()) { favourites, draws, today ->
            ProfileUiState(
                favourites = favourites.map { it.toListItem() },
                draws = draws.filter { it.draw.day.isoDate < today.isoDate }.map { it.toHistoryItem() },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProfileUiState())

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
                    imageUrls = breed.imageUrls,
                )
            }

            is FavouriteBreed.Unavailable -> {
                FavouriteListItem.Unavailable(breedId = breedId, name = name)
            }
        }

    private fun ResolvedDraw.toHistoryItem(): DrawHistoryItem =
        DrawHistoryItem(
            day = draw.day.isoDate,
            date = draw.day.toDisplayText(),
            breedId = draw.breedId,
            name = breed?.name ?: draw.name,
            imageUrls = breed?.imageUrls.orEmpty(),
            isAvailable = breed != null,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
