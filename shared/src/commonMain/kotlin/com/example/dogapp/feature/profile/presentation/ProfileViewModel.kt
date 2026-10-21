package com.example.dogapp.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.BreedSyncStatus
import com.example.dogapp.core.domain.draw.ObserveDrawHistoryUseCase
import com.example.dogapp.core.domain.draw.ResolvedDraw
import com.example.dogapp.core.domain.draw.TodayProvider
import com.example.dogapp.core.domain.favourite.FavouriteBreed
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.RemoveFavouriteUseCase
import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import com.example.dogapp.core.presentation.toDisplayText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class ProfileViewModel(
    observeFavouriteBreeds: ObserveFavouriteBreedsUseCase,
    observeDrawHistory: ObserveDrawHistoryUseCase,
    today: TodayProvider,
    private val removeFavourite: RemoveFavouriteUseCase,
    integrityChecker: DeviceIntegrityChecker,
    private val breedSync: BreedSyncCoordinator,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : ViewModel() {
    private val deviceStateWarnings =
        flow<Int?> { emit(integrityChecker.check().warningCount) }.onStart { emit(null) }

    private val syncData =
        combine(breedSync.status, breedSync.lastSyncedAt) { status, lastSyncedAt -> status.toSyncDataStatus(lastSyncedAt) }

    val state: StateFlow<ProfileUiState> =
        combine(
            observeFavouriteBreeds(),
            observeDrawHistory(),
            today.observeToday(),
            deviceStateWarnings,
            syncData,
        ) { favourites, draws, today, warnings, syncData ->
            ProfileUiState(
                favourites = favourites.map { it.toListItem() },
                draws = draws.filter { it.draw.day.isoDate < today.isoDate }.map { it.toHistoryItem() },
                deviceStateWarnings = warnings,
                syncData = syncData,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProfileUiState())

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.RemoveFavourite -> viewModelScope.launch { removeFavourite(action.breedId) }
            ProfileAction.SyncData -> breedSync.syncNow()
        }
    }

    private fun BreedSyncStatus.toSyncDataStatus(lastSyncedAt: Instant?): SyncDataStatus =
        when (this) {
            BreedSyncStatus.Syncing -> {
                SyncDataStatus.Syncing
            }

            is BreedSyncStatus.Failed -> {
                SyncDataStatus.Failed
            }

            is BreedSyncStatus.Done -> {
                if (unavailableSources.isEmpty()) {
                    SyncDataStatus.Idle(lastSyncedAt?.toDisplayText())
                } else {
                    SyncDataStatus.PartlySynced(unavailableSources)
                }
            }

            BreedSyncStatus.NotStarted -> {
                SyncDataStatus.Idle(lastSyncedAt?.toDisplayText())
            }
        }

    private fun Instant.toDisplayText(): String = toLocalDateTime(timeZone()).toDisplayText()

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
