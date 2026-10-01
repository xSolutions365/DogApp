package com.example.dogapp.core.presentation.components

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import com.example.dogapp.core.data.network.HttpClientFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DogImageTest {
    private val png = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
    private val requestedUrls = mutableListOf<String>()
    private val diskCache =
        DiskCache
            .Builder()
            .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "dog-image-test-${Random.nextLong()}")
            .maxSizeBytes(10_000_000)
            .build()
    private val imageLoader =
        ImageLoader
            .Builder(PlatformContext.INSTANCE)
            .components {
                add(
                    KtorNetworkFetcherFactory(
                        httpClient = {
                            HttpClientFactory.createForImages(
                                MockEngine { request ->
                                    requestedUrls += request.url.toString()
                                    respond(png, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
                                },
                            )
                        },
                    ),
                )
            }.diskCache { diskCache }
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()

    private fun load(
        url: String,
        failures: Int,
    ) = runBlocking {
        val attempt = dogImageAttempt(listOf(url), failures)
        imageLoader.execute(dogImageRequest(PlatformContext.INSTANCE, requireNotNull(attempt.url), attempt.bypassCache))
    }

    @Test
    fun aPhotoWhoseCachedCopyIsEmptyFailsFromCacheThenTheRetryRefetchesItAndRepairsTheCache() {
        val url = "https://images.example.test/andalusian.jpg"
        assertIs<SuccessResult>(load(url, failures = 0))
        val cachedData = requireNotNull(diskCache.openSnapshot(url)).use { it.data }
        FileSystem.SYSTEM.write(cachedData) {}
        requestedUrls.clear()

        assertIs<ErrorResult>(load(url, failures = 0))
        assertEquals(emptyList(), requestedUrls)

        assertIs<SuccessResult>(load(url, failures = 1))
        assertEquals(listOf(url), requestedUrls)
        assertEquals(png.size.toLong(), diskCache.openSnapshot(url)?.use { FileSystem.SYSTEM.metadata(it.data).size })

        requestedUrls.clear()
        assertIs<SuccessResult>(load(url, failures = 0))
        assertEquals(emptyList(), requestedUrls)
    }
}
