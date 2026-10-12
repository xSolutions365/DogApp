package com.example.dogapp.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.dogapp.core.data.breed.local.SourcedBreedDao
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.data.draw.local.DailyDrawDao
import com.example.dogapp.core.data.favourite.local.FavouriteBreedDao
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.BreedSyncCoordinator
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
import com.example.dogapp.core.domain.draw.DailyDrawRepository
import com.example.dogapp.core.domain.draw.DrawBreedOfTheDayUseCase
import com.example.dogapp.core.domain.draw.ObserveDrawHistoryUseCase
import com.example.dogapp.core.domain.draw.ObserveTodaysDrawUseCase
import com.example.dogapp.core.domain.draw.TodayProvider
import com.example.dogapp.core.domain.favourite.FavouritesRepository
import com.example.dogapp.core.domain.favourite.ObserveFavouriteBreedsUseCase
import com.example.dogapp.core.domain.favourite.ToggleFavouriteUseCase
import com.example.dogapp.feature.donate.domain.CharityRepository
import com.example.dogapp.feature.identify.domain.BreedClassifier
import com.example.dogapp.feature.identify.domain.IdentifyBreedUseCase
import io.ktor.client.HttpClient
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinGraphTest : KoinTest {
    @BeforeTest
    fun setUp() {
        initKoin()
    }

    @AfterTest
    fun tearDown() {
        get<DogAppDatabase>().close()
        stopKoin()
    }

    @Test
    fun appModulesResolveEveryDependencyWithThePlatformModule() {
        assertNotNull(get<HttpClient>().engine)
        assertNotNull(get<DataStore<Preferences>>())
        assertNotNull(get<DogAppDatabase>())
        assertNotNull(get<SourcedBreedDao>())
        assertNotNull(get<BreedRepository>())
        assertNotNull(get<SyncBreedsUseCase>())
        assertNotNull(get<FavouriteBreedDao>())
        assertNotNull(get<FavouritesRepository>())
        assertNotNull(get<ObserveFavouriteBreedsUseCase>())
        assertNotNull(get<ToggleFavouriteUseCase>())
        assertNotNull(get<BreedSyncCoordinator>())
        assertNotNull(get<DailyDrawDao>())
        assertNotNull(get<DailyDrawRepository>())
        assertNotNull(get<TodayProvider>())
        assertNotNull(get<DrawBreedOfTheDayUseCase>())
        assertNotNull(get<ObserveTodaysDrawUseCase>())
        assertNotNull(get<ObserveDrawHistoryUseCase>())
        assertNotNull(get<BreedClassifier>())
        assertNotNull(get<IdentifyBreedUseCase>())
        assertNotNull(get<CharityRepository>())
    }
}
