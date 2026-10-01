package com.example.dogapp.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.ic_dog_placeholder
import org.jetbrains.compose.resources.painterResource

private const val PLACEHOLDER_SIZE_FRACTION = 0.4f

@Composable
fun DogImage(
    urls: List<String>,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
    onLoadFailure: () -> Unit = {},
) {
    val currentOnLoadFailure by rememberUpdatedState(onLoadFailure)
    var failures by remember(urls) { mutableIntStateOf(0) }
    val attempt = dogImageAttempt(urls, failures)
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_dog_placeholder),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(PLACEHOLDER_SIZE_FRACTION),
        )
        val url = attempt.url
        if (url != null) {
            val context = LocalPlatformContext.current
            key(url, attempt.bypassCache) {
                AsyncImage(
                    model = remember { dogImageRequest(context, url, attempt.bypassCache) },
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        failures++
                        if (dogImageAttempt(urls, failures).isExhausted) currentOnLoadFailure()
                    },
                )
            }
        }
    }
}
