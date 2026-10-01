package com.example.dogapp.app.image

import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImageLoaderFactoryTest {
    private val onePixelPng =
        Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")

    @Test
    fun imageUrlsAreDownloadedThroughKtorAndDecoded() =
        runTest {
            val requestedUrls = mutableListOf<String>()
            val engine =
                MockEngine { request ->
                    requestedUrls += request.url.toString()
                    respond(onePixelPng, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "image/png"))
                }
            val imageLoader = createImageLoader(PlatformContext.INSTANCE, engine)

            val result =
                imageLoader.execute(
                    ImageRequest
                        .Builder(PlatformContext.INSTANCE)
                        .data("https://images.example.test/border-collie.png")
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                )

            val success = assertIs<SuccessResult>(result)
            assertEquals(1, success.image.width)
            assertEquals(1, success.image.height)
            assertEquals(listOf("https://images.example.test/border-collie.png"), requestedUrls)
        }
}
