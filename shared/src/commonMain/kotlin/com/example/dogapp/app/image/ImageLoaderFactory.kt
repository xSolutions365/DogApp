package com.example.dogapp.app.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.example.dogapp.app.logging.KermitCoilLogger
import com.example.dogapp.core.data.network.HttpClientFactory
import com.example.dogapp.core.presentation.theme.DogAppMotion
import io.ktor.client.engine.HttpClientEngine

fun createImageLoader(
    context: PlatformContext,
    engine: HttpClientEngine,
): ImageLoader =
    ImageLoader
        .Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(httpClient = { HttpClientFactory.createForImages(engine) }))
        }.crossfade(DogAppMotion.DURATION_MEDIUM2_MILLIS)
        .logger(KermitCoilLogger())
        .build()
