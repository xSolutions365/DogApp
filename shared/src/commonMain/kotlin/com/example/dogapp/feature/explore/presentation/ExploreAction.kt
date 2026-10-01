package com.example.dogapp.feature.explore.presentation

sealed interface ExploreAction {
    data class QueryChange(
        val query: String,
    ) : ExploreAction

    data object Retry : ExploreAction
}
