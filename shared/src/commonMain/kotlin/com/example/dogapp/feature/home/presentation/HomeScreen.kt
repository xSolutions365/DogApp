package com.example.dogapp.feature.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.animation.breedImageSharedKey
import com.example.dogapp.core.presentation.animation.navSharedElement
import com.example.dogapp.core.presentation.components.DogImage
import com.example.dogapp.core.presentation.components.ErrorState
import com.example.dogapp.core.presentation.components.LoadingState
import com.example.dogapp.core.presentation.theme.DogAppMotion
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.breed_unavailable
import dogapp.shared.generated.resources.breeds_empty
import dogapp.shared.generated.resources.breeds_error_title
import dogapp.shared.generated.resources.breeds_loading_body
import dogapp.shared.generated.resources.breeds_loading_title
import dogapp.shared.generated.resources.breeds_retry
import dogapp.shared.generated.resources.home_draw_hint
import dogapp.shared.generated.resources.home_draw_reveal
import dogapp.shared.generated.resources.home_draw_title
import dogapp.shared.generated.resources.home_identify
import dogapp.shared.generated.resources.home_learn_more
import dogapp.shared.generated.resources.home_revealed_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val CARD_ASPECT_RATIO = 0.8f

@Composable
fun HomeRoot(
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, onAction = viewModel::onAction, onBreedClick = onBreedClick, onIdentifyClick = onIdentifyClick)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val content = state.content) {
        HomeContent.Pending -> {
            Box(modifier.fillMaxSize())
        }

        HomeContent.Loading -> {
            LoadingState(
                title = stringResource(Res.string.breeds_loading_title),
                body = stringResource(Res.string.breeds_loading_body),
                modifier = modifier,
            )
        }

        is HomeContent.Error -> {
            ErrorState(
                title = stringResource(Res.string.breeds_error_title),
                message = stringResource(content.message),
                retryLabel = stringResource(Res.string.breeds_retry),
                onRetry = { onAction(HomeAction.Retry) },
                modifier = modifier,
            )
        }

        HomeContent.NoBreeds -> {
            Text(
                text = stringResource(Res.string.breeds_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        is HomeContent.NotRevealed, is HomeContent.Revealed -> {
            DrawSection(
                revealed = (content as? HomeContent.Revealed)?.breed,
                isDrawing = (content as? HomeContent.NotRevealed)?.isDrawing == true,
                onAction = onAction,
                onBreedClick = onBreedClick,
                onIdentifyClick = onIdentifyClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun DrawSection(
    revealed: DrawnBreed?,
    isDrawing: Boolean,
    onAction: (HomeAction) -> Unit,
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(if (revealed == null) Res.string.home_draw_title else Res.string.home_revealed_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth().semantics { heading() },
        )
        AnimatedVisibility(
            visible = revealed == null,
            exit = fadeOut(tween(DogAppMotion.DURATION_SHORT2_MILLIS)) + shrinkVertically(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS)),
        ) {
            RevealButton(isDrawing = isDrawing, onReveal = { onAction(HomeAction.Reveal) }, modifier = Modifier.padding(top = 24.dp))
        }
        AnimatedVisibility(
            visible = revealed != null,
            enter =
                expandVertically(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS), expandFrom = Alignment.Top) +
                    slideInVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow, IntOffset.VisibilityThreshold)) { -it } +
                    fadeIn(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS)),
        ) {
            revealed?.let { breed ->
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BreedOfTheDayCard(
                        breed = breed,
                        onBreedClick = onBreedClick,
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(CARD_ASPECT_RATIO),
                    )
                    Text(
                        text = stringResource(Res.string.home_draw_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        OutlinedButton(onClick = onIdentifyClick, modifier = Modifier.padding(top = 32.dp)) {
            Text(stringResource(Res.string.home_identify))
        }
    }
}

@Composable
private fun RevealButton(
    isDrawing: Boolean,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onReveal,
        enabled = !isDrawing,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
        modifier = modifier,
    ) {
        if (isDrawing) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                strokeWidth = 2.dp,
                modifier = Modifier.padding(end = 8.dp).size(16.dp),
            )
        }
        Text(stringResource(Res.string.home_draw_reveal))
    }
}

@Composable
private fun BreedOfTheDayCard(
    breed: DrawnBreed,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickModifier =
        when (breed) {
            is DrawnBreed.Available -> Modifier.clickable(onClick = { onBreedClick(breed.breedId) })
            is DrawnBreed.Unavailable -> Modifier
        }
    Card(
        modifier = modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)).then(clickModifier),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        DogImage(
            urls = (breed as? DrawnBreed.Available)?.imageUrls.orEmpty(),
            modifier =
                Modifier
                    .then(if (breed is DrawnBreed.Available) Modifier.navSharedElement(breedImageSharedKey(breed.breedId)) else Modifier)
                    .fillMaxWidth()
                    .weight(1f),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = breed.name, style = MaterialTheme.typography.headlineSmall)
            when (breed) {
                is DrawnBreed.Available -> {
                    breed.subtitle?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = stringResource(Res.string.home_learn_more),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                is DrawnBreed.Unavailable -> {
                    Text(
                        text = stringResource(Res.string.breed_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
