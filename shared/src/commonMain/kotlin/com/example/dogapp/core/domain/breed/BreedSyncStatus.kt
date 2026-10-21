package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError

sealed interface BreedSyncStatus {
    data object NotStarted : BreedSyncStatus

    data object Syncing : BreedSyncStatus

    data class Done(
        val unavailableSources: List<String>,
    ) : BreedSyncStatus

    data class Failed(
        val error: DataError.Remote,
    ) : BreedSyncStatus
}
