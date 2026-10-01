package com.example.dogapp.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.dogapp.core.data.breed.local.SourcedBreedDao
import com.example.dogapp.core.data.database.DogAppDatabase
import com.example.dogapp.core.domain.breed.BreedRepository
import com.example.dogapp.core.domain.breed.SyncBreedsUseCase
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
    }
}
