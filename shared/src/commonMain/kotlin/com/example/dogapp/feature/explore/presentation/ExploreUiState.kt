package com.example.dogapp.feature.explore.presentation

import com.example.dogapp.feature.explore.domain.BreedFilters
import com.example.dogapp.feature.explore.domain.BreedInfo
import com.example.dogapp.feature.explore.domain.FilterCategory
import org.jetbrains.compose.resources.StringResource

data class ExploreUiState(
    val query: String = "",
    val filters: BreedFilters = BreedFilters(),
    val availableFilters: Set<FilterCategory> = emptySet(),
    val groupOptions: List<String> = emptyList(),
    val content: ExploreContent = ExploreContent.Pending,
) {
    val isSearchEnabled: Boolean
        get() = content is ExploreContent.Results || content is ExploreContent.NoMatches
}

sealed interface ExploreContent {
    data object Pending : ExploreContent

    data object Loading : ExploreContent

    data class Error(
        val message: StringResource,
    ) : ExploreContent

    data class Results(
        val breeds: List<BreedListItem>,
        val hiddenForMissingInfo: Map<BreedInfo, Int> = emptyMap(),
    ) : ExploreContent

    data class NoMatches(
        val query: String,
        val hasFilters: Boolean = false,
        val hiddenForMissingInfo: Map<BreedInfo, Int> = emptyMap(),
    ) : ExploreContent
}

data class BreedListItem(
    val id: String,
    val name: String,
    val subtitle: String?,
    val imageUrls: List<String>,
    val isFavourite: Boolean,
)
