package com.example.dogapp.feature.profile.presentation

sealed interface ProfileAction {
    data class RemoveFavourite(
        val breedId: String,
    ) : ProfileAction

    data object SyncData : ProfileAction
}
