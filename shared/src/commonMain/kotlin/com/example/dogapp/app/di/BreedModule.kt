package com.example.dogapp.app.di

import com.example.dogapp.core.data.breed.BreedImageSource
import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.breed.DefaultBreedRepository
import com.example.dogapp.core.data.breed.apininjas.ApiNinjasBreedSource
import com.example.dogapp.core.data.breed.dogceo.DogCeoBreedSource
import com.example.dogapp.core.data.breed.thedogapi.TheDogApiBreedSource
import com.example.dogapp.core.data.config.BuildSecrets
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.GetBreedGalleryUseCase
import com.example.dogapp.core.domain.breed.ObserveBreedUseCase
import com.example.dogapp.core.domain.breed.ObserveBreedsUseCase
import com.example.dogapp.core.domain.breed.SearchBreedsUseCase
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

val breedModule =
    module {
        single { TheDogApiBreedSource(get(), BuildSecrets.THE_DOG_API_KEY) } binds arrayOf(BreedSource::class, BreedImageSource::class)
        single { DogCeoBreedSource(get()) } binds arrayOf(BreedSource::class, BreedImageSource::class)
        single { ApiNinjasBreedSource(get(), BuildSecrets.API_NINJAS_KEY) } bind BreedSource::class
        single<BreedRepository> { DefaultBreedRepository(sources = getAll(), imageSources = getAll(), dao = get()) }
        factoryOf(::SyncBreedsUseCase)
        singleOf(::BreedSyncCoordinator)
        factoryOf(::ObserveBreedsUseCase)
        factoryOf(::SearchBreedsUseCase)
        factoryOf(::ObserveBreedUseCase)
        factoryOf(::GetBreedGalleryUseCase)
    }
