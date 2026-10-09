package com.example.dogapp.feature.identify.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogapp.core.domain.onFailure
import com.example.dogapp.core.domain.onSuccess
import com.example.dogapp.feature.identify.domain.BreedIdentification
import com.example.dogapp.feature.identify.domain.IdentifiedBreed
import com.example.dogapp.feature.identify.domain.IdentifyBreedUseCase
import com.example.dogapp.feature.identify.domain.IdentifyError
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.identify_error_model
import dogapp.shared.generated.resources.identify_error_unreadable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class IdentifyViewModel(
    private val identifyBreed: IdentifyBreedUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(IdentifyUiState())
    val state: StateFlow<IdentifyUiState> = mutableState.asStateFlow()
    private var identifyJob: Job? = null

    fun onAction(action: IdentifyAction) {
        when (action) {
            is IdentifyAction.PhotoPick -> identify(action.photo)
        }
    }

    private fun identify(photo: ByteArray) {
        identifyJob?.cancel()
        mutableState.value = IdentifyUiState(photo = photo, content = IdentifyContent.Identifying)
        identifyJob =
            viewModelScope.launch {
                identifyBreed(photo)
                    .onSuccess { identification -> mutableState.value = IdentifyUiState(photo, identification.toContent()) }
                    .onFailure { error -> mutableState.value = IdentifyUiState(photo, IdentifyContent.Error(error.toStringResource())) }
            }
    }

    private fun BreedIdentification.toContent(): IdentifyContent =
        when (this) {
            is BreedIdentification.Confident -> IdentifyContent.Matches(isConfident = true, matches = listOf(match.toBreedMatch()))
            is BreedIdentification.Uncertain -> IdentifyContent.Matches(isConfident = false, matches = candidates.map { it.toBreedMatch() })
            BreedIdentification.NoDog -> IdentifyContent.NoDog
        }

    private fun IdentifiedBreed.toBreedMatch(): BreedMatch =
        BreedMatch(
            breedId = breed?.id,
            name = name,
            confidencePercent = (confidence * PERCENT).roundToInt(),
            imageUrls = breed?.imageUrls.orEmpty(),
        )

    private fun IdentifyError.toStringResource() =
        when (this) {
            IdentifyError.UnreadablePhoto -> Res.string.identify_error_unreadable
            IdentifyError.ModelUnavailable -> Res.string.identify_error_model
        }

    private companion object {
        const val PERCENT = 100
    }
}
