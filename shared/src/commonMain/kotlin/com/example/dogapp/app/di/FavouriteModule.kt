package com.example.dogapp.app.di

import com.example.dogapp.core.data.favourite.DefaultFavouritesRepository
import com.example.dogapp.core.domain.favourite.FavouritesRepository
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.ObserveFavouriteIdsUseCase
import com.example.dogapp.core.domain.favourite.ObserveIsFavouriteUseCase
import com.example.dogapp.core.domain.favourite.RemoveFavouriteUseCase
import com.example.dogapp.core.domain.favourite.ToggleFavouriteUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val favouriteModule =
    module {
        single<FavouritesRepository> { DefaultFavouritesRepository(dao = get()) }
        factoryOf(::ObserveFavouriteBreedsUseCase)
        factoryOf(::ObserveFavouriteIdsUseCase)
        factoryOf(::ObserveIsFavouriteUseCase)
        factoryOf(::ToggleFavouriteUseCase)
        factoryOf(::RemoveFavouriteUseCase)
    }
