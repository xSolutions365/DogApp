package com.example.dogapp.feature.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.animation.IDENTIFY_SHARED_KEY
import com.example.dogapp.core.presentation.animation.breedImageSharedKey
import com.example.dogapp.core.presentation.animation.navSharedBounds
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
import dogapp.shared.generated.resources.home_draw_tap
import dogapp.shared.generated.resources.home_draw_title
import dogapp.shared.generated.resources.home_identify
import dogapp.shared.generated.resources.home_identify_action
import dogapp.shared.generated.resources.home_learn_more
import dogapp.shared.generated.resources.home_revealed_title
import dogapp.shared.generated.resources.ic_dog_placeholder
import dogapp.shared.generated.resources.ic_image_search
import dogapp.shared.generated.resources.identify_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val CARD_ASPECT_RATIO = 0.8f
private const val CARD_BACK_ICON_FRACTION = 0.4f
private const val FLIPPED_DEGREES = 180f
private const val HALF_FLIP_DEGREES = 90f
private const val FLIP_DURATION_MILLIS = 600
private const val CAMERA_DISTANCE = 12f
private val CardShape = RoundedCornerShape(28.dp)
private val FAB_CLEARANCE = 88.dp

@Composable
fun HomeRoot(
    layout: HomeLayout,
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        layout = layout,
        onAction = viewModel::onAction,
        onBreedClick = onBreedClick,
        onIdentifyClick = onIdentifyClick,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    layout: HomeLayout,
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
            val revealed = (content as? HomeContent.Revealed)?.breed
            val isDrawing = (content as? HomeContent.NotRevealed)?.isDrawing == true
            when (layout) {
                HomeLayout.Material -> {
                    MaterialHome(
                        revealed = revealed,
                        isDrawing = isDrawing,
                        onAction = onAction,
                        onBreedClick = onBreedClick,
                        onIdentifyClick = onIdentifyClick,
                        modifier = modifier,
                    )
                }

                HomeLayout.Cupertino -> {
                    CupertinoHome(
                        revealed = revealed,
                        isDrawing = isDrawing,
                        onAction = onAction,
                        onBreedClick = onBreedClick,
                        onIdentifyClick = onIdentifyClick,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialHome(
    revealed: DrawnBreed?,
    isDrawing: Boolean,
    onAction: (HomeAction) -> Unit,
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Box(modifier = modifier.fillMaxSize()) {
        DrawSection(
            revealed = revealed,
            isDrawing = isDrawing,
            onAction = onAction,
            onBreedClick = onBreedClick,
            scrollState = scrollState,
            modifier = Modifier.padding(bottom = FAB_CLEARANCE),
        )
        ExtendedFloatingActionButton(
            text = { Text(stringResource(Res.string.identify_title)) },
            icon = { Icon(painterResource(Res.drawable.ic_image_search), contentDescription = stringResource(Res.string.home_identify)) },
            expanded = scrollState.value == 0,
            onClick = onIdentifyClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navSharedBounds(IDENTIFY_SHARED_KEY, FloatingActionButtonDefaults.extendedFabShape),
        )
    }
}

@Composable
private fun CupertinoHome(
    revealed: DrawnBreed?,
    isDrawing: Boolean,
    onAction: (HomeAction) -> Unit,
    onBreedClick: (String) -> Unit,
    onIdentifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onIdentifyClick) {
                Text(stringResource(Res.string.home_identify_action), style = MaterialTheme.typography.titleMedium)
            }
        }
        DrawSection(
            revealed = revealed,
            isDrawing = isDrawing,
            onAction = onAction,
            onBreedClick = onBreedClick,
            scrollState = rememberScrollState(),
        )
    }
}

@Composable
private fun DrawSection(
    revealed: DrawnBreed?,
    isDrawing: Boolean,
    onAction: (HomeAction) -> Unit,
    onBreedClick: (String) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(if (revealed == null) Res.string.home_draw_title else Res.string.home_revealed_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth().semantics { heading() },
        )
        RevealCard(
            revealed = revealed,
            isDrawing = isDrawing,
            onReveal = { onAction(HomeAction.Reveal) },
            onBreedClick = onBreedClick,
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(CARD_ASPECT_RATIO),
        )
        AnimatedVisibility(visible = revealed != null, enter = fadeIn(tween(DogAppMotion.DURATION_MEDIUM2_MILLIS))) {
            Text(
                text = stringResource(Res.string.home_draw_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RevealCard(
    revealed: DrawnBreed?,
    isDrawing: Boolean,
    onReveal: () -> Unit,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val rotation by animateFloatAsState(
        targetValue = if (revealed != null) FLIPPED_DEGREES else 0f,
        animationSpec = tween(FLIP_DURATION_MILLIS, easing = DogAppMotion.EmphasizedEasing),
    )
    Box(
        modifier =
            modifier.graphicsLayer {
                rotationY = rotation
                cameraDistance = CAMERA_DISTANCE * density
            },
    ) {
        if (revealed != null && rotation > HALF_FLIP_DEGREES) {
            BreedOfTheDayCard(
                breed = revealed,
                onBreedClick = onBreedClick,
                modifier = Modifier.graphicsLayer { rotationY = FLIPPED_DEGREES },
            )
        } else {
            CardBack(
                isDrawing = isDrawing,
                onReveal = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onReveal()
                },
            )
        }
    }
}

@Composable
private fun CardBack(
    isDrawing: Boolean,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize().clip(CardShape).clickable(enabled = !isDrawing, onClick = onReveal),
        shape = CardShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth(CARD_BACK_ICON_FRACTION).aspectRatio(1f), contentAlignment = Alignment.Center) {
                if (isDrawing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onTertiaryContainer)
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_dog_placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Text(
                text = stringResource(Res.string.home_draw_reveal),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.home_draw_tap),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
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
        modifier = modifier.fillMaxSize().clip(CardShape).then(clickModifier),
        shape = CardShape,
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
