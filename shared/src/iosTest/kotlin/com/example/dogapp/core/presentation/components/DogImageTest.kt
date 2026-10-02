package com.example.dogapp.core.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
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
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DogImageTest {
    private val png =
        Base64.decode(
            "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAZUlEQVR42u3QQREAAAQAMHFEFFErcjh7rMCiK+ezECBAgAABAgQIECBAgAABAgQIECBAgAABAgQIECBAgAABAgQIECBAgAABAgQIECBAgAABAgQIECBAgAABAgQIECBAgAABAu5b9l1yOyG8Se8AAAAASUVORK5CYII=",
        )
    private val requestedUrls = mutableListOf<String>()
    private val diskCache =
        DiskCache
            .Builder()
            .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "dog-image-test-${Random.nextLong()}")
            .maxSizeBytes(10_000_000)
            .build()
    private val imageLoader = imageLoader(memoryCachePolicy = CachePolicy.DISABLED)

    private fun imageLoader(memoryCachePolicy: CachePolicy) =
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
            .memoryCachePolicy(memoryCachePolicy)
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

    @Test
    fun aShownPhotoIsDownloadedOnceAndItsBackdropIsKeptAsASeparateSmallImage() =
        runComposeUiTest {
            val url = "https://images.example.test/beagle.jpg"
            val loader = imageLoader(memoryCachePolicy = CachePolicy.ENABLED)
            val memoryCache = requireNotNull(loader.memoryCache)

            setContent { DogImage(urls = listOf(url), imageLoader = loader, modifier = Modifier.size(48.dp)) }
            waitUntil(timeoutMillis = 5_000) { memoryCache.keys.size == 2 }

            assertEquals(listOf(url), requestedUrls)
            val (backdrop, photo) = memoryCache.keys.map { requireNotNull(memoryCache[it]).image }.sortedBy { it.width }
            assertTrue(backdrop.width < photo.width)
            assertTrue(backdrop.height < photo.height)
        }
}
