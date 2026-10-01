package com.example.dogapp.feature.home.presentation

sealed interface HomeAction {
    data object Reveal : HomeAction

    data object Retry : HomeAction
}
