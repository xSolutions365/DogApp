package com.example.dogapp.feature.explore.presentation

import com.example.dogapp.feature.explore.domain.BreedFilters

sealed interface ExploreAction {
    data class QueryChange(
        val query: String,
    ) : ExploreAction

    data class FiltersChange(
        val filters: BreedFilters,
    ) : ExploreAction

    data object Retry : ExploreAction
}
