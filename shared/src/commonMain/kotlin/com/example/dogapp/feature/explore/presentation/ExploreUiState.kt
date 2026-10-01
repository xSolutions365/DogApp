package com.example.dogapp.feature.explore.presentation

import org.jetbrains.compose.resources.StringResource

data class ExploreUiState(
    val query: String = "",
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
    ) : ExploreContent

    data class NoMatches(
        val query: String,
    ) : ExploreContent
}

data class BreedListItem(
    val id: String,
    val name: String,
    val subtitle: String?,
    val imageUrl: String?,
)
