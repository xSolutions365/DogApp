package com.example.dogapp.di

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
        stopKoin()
    }

    @Test
    fun appModulesResolveHttpClientWithPlatformEngine() {
        assertNotNull(get<HttpClient>().engine)
    }
}
