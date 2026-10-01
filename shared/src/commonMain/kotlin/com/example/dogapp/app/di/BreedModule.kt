package com.example.dogapp.app.di

import com.example.dogapp.core.data.breed.BreedSource
import com.example.dogapp.core.data.breed.DefaultBreedRepository
import com.example.dogapp.core.data.breed.apininjas.ApiNinjasBreedSource
import com.example.dogapp.core.data.breed.dogceo.DogCeoBreedSource
import com.example.dogapp.core.data.breed.thedogapi.TheDogApiBreedSource
import com.example.dogapp.core.data.config.BuildSecrets
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val breedModule =
    module {
        single { TheDogApiBreedSource(get(), BuildSecrets.THE_DOG_API_KEY) } bind BreedSource::class
        single { DogCeoBreedSource(get()) } bind BreedSource::class
        single { ApiNinjasBreedSource(get(), BuildSecrets.API_NINJAS_KEY) } bind BreedSource::class
        single<BreedRepository> { DefaultBreedRepository(getAll()) }
        factoryOf(::SyncBreedsUseCase)
    }
