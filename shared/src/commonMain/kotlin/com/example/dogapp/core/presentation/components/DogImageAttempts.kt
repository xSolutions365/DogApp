package com.example.dogapp.core.presentation.components

import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest

private const val ATTEMPTS_PER_URL = 2

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
