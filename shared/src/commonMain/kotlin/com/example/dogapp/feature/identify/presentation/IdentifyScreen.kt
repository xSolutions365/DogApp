package com.example.dogapp.feature.identify.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.dogapp.core.presentation.components.DogImage
import com.example.dogapp.core.presentation.components.platformBackIcon
import com.example.dogapp.core.presentation.theme.DogAppMotion
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.detail_back
import dogapp.shared.generated.resources.ic_dog_placeholder
import dogapp.shared.generated.resources.identify_choose_another
import dogapp.shared.generated.resources.identify_choose_photo
import dogapp.shared.generated.resources.identify_confident
import dogapp.shared.generated.resources.identify_done
import dogapp.shared.generated.resources.identify_intro
import dogapp.shared.generated.resources.identify_match
import dogapp.shared.generated.resources.identify_no_dog
import dogapp.shared.generated.resources.identify_photo
import dogapp.shared.generated.resources.identify_title
import dogapp.shared.generated.resources.identify_uncertain
import dogapp.shared.generated.resources.identify_working
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val PHOTO_ASPECT_RATIO = 4f / 3f
private const val PLACEHOLDER_SIZE_FRACTION = 0.3f

@Composable
fun IdentifyRoot(
    presentation: IdentifyPresentation,
    onBreedClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IdentifyViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pickPhoto = rememberPhotoPicker { photo -> viewModel.onAction(IdentifyAction.PhotoPick(photo)) }
    IdentifyScreen(
        state = state,
        presentation = presentation,
        onChoosePhoto = pickPhoto,
        onBreedClick = onBreedClick,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
fun IdentifyScreen(
    state: IdentifyUiState,
    presentation: IdentifyPresentation,
    onChoosePhoto: () -> Unit,
    onBreedClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(presentation = presentation, onDismiss = onDismiss)
        PhotoFrame(photo = state.photo, modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(PHOTO_ASPECT_RATIO))
        Button(onClick = onChoosePhoto, enabled = state.content != IdentifyContent.Identifying) {
            Text(stringResource(if (state.photo == null) Res.string.identify_choose_photo else Res.string.identify_choose_another))
        }
        AnimatedContent(
            targetState = state.content,
            transitionSpec = {
                fadeIn(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS)) togetherWith fadeOut(tween(DogAppMotion.DURATION_SHORT2_MILLIS))
            },
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        ) { content ->
            IdentifyResult(content = content, onBreedClick = onBreedClick)
        }
    }
}

@Composable
private fun TopBar(
    presentation: IdentifyPresentation,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (presentation == IdentifyPresentation.Screen) {
            IconButton(onClick = onDismiss) {
                Icon(painterResource(platformBackIcon), contentDescription = stringResource(Res.string.detail_back))
            }
        }
        Text(
            text = stringResource(Res.string.identify_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        if (presentation == IdentifyPresentation.Sheet) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.identify_done), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PhotoFrame(
    photo: ByteArray?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (photo == null) {
                Icon(
                    painter = painterResource(Res.drawable.ic_dog_placeholder),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxSize(PLACEHOLDER_SIZE_FRACTION),
                )
            } else {
                AsyncImage(
                    model = photo,
                    contentDescription = stringResource(Res.string.identify_photo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun IdentifyResult(
    content: IdentifyContent,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (content) {
            IdentifyContent.Empty -> {
                ResultMessage(stringResource(Res.string.identify_intro))
            }

            IdentifyContent.Identifying -> {
                CircularProgressIndicator()
                ResultMessage(stringResource(Res.string.identify_working))
            }

            IdentifyContent.NoDog -> {
                ResultMessage(stringResource(Res.string.identify_no_dog))
            }

            is IdentifyContent.Error -> {
                ResultMessage(stringResource(content.message))
            }

            is IdentifyContent.Matches -> {
                Text(
                    text = stringResource(if (content.isConfident) Res.string.identify_confident else Res.string.identify_uncertain),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                content.matches.forEach { match -> MatchRow(match = match, onBreedClick = onBreedClick) }
            }
        }
    }
}

@Composable
private fun ResultMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun MatchRow(
    match: BreedMatch,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val breedId = match.breedId
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .then(if (breedId != null) Modifier.clickable(onClick = { onBreedClick(breedId) }) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (breedId != null) {
                DogImage(urls = match.imageUrls, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = match.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(Res.string.identify_match, match.confidencePercent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
