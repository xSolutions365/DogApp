package com.example.dogapp.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.ic_dog_placeholder
import org.jetbrains.compose.resources.painterResource

private const val PLACEHOLDER_SIZE_FRACTION = 0.4f

@Composable
fun DogImage(
    url: String?,
    modifier: Modifier = Modifier,
) {
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
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
