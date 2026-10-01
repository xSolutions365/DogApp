package com.example.dogapp.app.di

import com.example.dogapp.core.data.draw.DefaultDailyDrawRepository
import com.example.dogapp.core.data.draw.SystemTodayProvider
import com.example.dogapp.core.domain.draw.DailyDrawRepository
import com.example.dogapp.core.domain.draw.DrawBreedOfTheDayUseCase
import com.example.dogapp.core.domain.draw.ObserveDrawHistoryUseCase
import com.example.dogapp.core.domain.draw.ObserveTodaysDrawUseCase
import com.example.dogapp.core.domain.draw.TodayProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val drawModule =
    module {
        single<DailyDrawRepository> { DefaultDailyDrawRepository(dao = get()) }
        single<TodayProvider> { SystemTodayProvider() }
        factory { DrawBreedOfTheDayUseCase(draws = get(), breeds = get(), today = get()) }
        factoryOf(::ObserveTodaysDrawUseCase)
        factoryOf(::ObserveDrawHistoryUseCase)
    }
