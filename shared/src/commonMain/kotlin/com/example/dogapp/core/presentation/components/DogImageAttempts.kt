package com.example.dogapp.core.presentation.components

import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest

private const val ATTEMPTS_PER_URL = 2
private const val BACKDROP_SIZE_PX = 24
private const val BACKDROP_CACHE_KEY_EXTRA = "dogImageBackdrop"

data class DogImageAttempt(
    val url: String?,
    val bypassCache: Boolean,
) {
    val isExhausted: Boolean get() = url == null
}

fun dogImageAttempt(
    urls: List<String>,
    failures: Int,
): DogImageAttempt =
    DogImageAttempt(
        url = urls.getOrNull(failures / ATTEMPTS_PER_URL),
        bypassCache = failures % ATTEMPTS_PER_URL == 1,
    )

fun dogImageRequest(
    context: PlatformContext,
    url: String,
    bypassCache: Boolean,
): ImageRequest =
    ImageRequest
        .Builder(context)
        .data(url)
        .apply {
            if (bypassCache) {
                diskCachePolicy(CachePolicy.WRITE_ONLY)
                memoryCachePolicy(CachePolicy.WRITE_ONLY)
            }
        }.build()

fun dogImageBackdropRequest(
    context: PlatformContext,
    url: String,
): ImageRequest =
    ImageRequest
        .Builder(context)
        .data(url)
        .size(BACKDROP_SIZE_PX)
        .memoryCacheKeyExtra(BACKDROP_CACHE_KEY_EXTRA, BACKDROP_SIZE_PX.toString())
        .build()
