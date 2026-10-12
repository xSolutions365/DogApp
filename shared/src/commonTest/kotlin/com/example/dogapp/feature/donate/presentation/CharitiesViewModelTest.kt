package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.testing.FakeCharityRepository
import com.example.dogapp.testing.testCharity
import kotlin.test.Test
import kotlin.test.assertEquals

class CharitiesViewModelTest {
    private val repository = FakeCharityRepository(listOf(testCharity("wag-street"), testCharity("fetch-club")))

    @Test
    fun listShowsEveryCharityInRepositoryOrder() {
        val viewModel = CharitiesViewModel(repository)

        assertEquals(
            listOf("wag-street", "fetch-club"),
            viewModel.state.value.charities
                .map { it.id },
        )
    }

    @Test
    fun detailShowsTheRequestedCharityOrNotFound() {
        assertEquals(CharityUiState.Loaded(testCharity("fetch-club")), CharityViewModel("fetch-club", repository).state.value)
        assertEquals(CharityUiState.NotFound, CharityViewModel("closed-down", repository).state.value)
    }
}
