package com.example.dogapp.feature.explore.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.animation.breedImageSharedKey
import com.example.dogapp.core.presentation.animation.navSharedElement
import com.example.dogapp.core.presentation.components.DogImage
import com.example.dogapp.core.presentation.components.ErrorState
import com.example.dogapp.core.presentation.components.LoadingState
import com.example.dogapp.core.presentation.components.SearchField
import com.example.dogapp.feature.explore.domain.BreedInfo
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.breeds_empty
import dogapp.shared.generated.resources.breeds_error_title
import dogapp.shared.generated.resources.breeds_loading_body
import dogapp.shared.generated.resources.breeds_loading_title
import dogapp.shared.generated.resources.breeds_retry
import dogapp.shared.generated.resources.explore_favourite_indicator
import dogapp.shared.generated.resources.explore_no_filter_matches
import dogapp.shared.generated.resources.explore_no_matches
import dogapp.shared.generated.resources.explore_no_search_filter_matches
import dogapp.shared.generated.resources.explore_search_placeholder
import dogapp.shared.generated.resources.ic_favorite_filled
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val HIDDEN_NOTE_KEY = "hidden-note"

@Composable
fun ExploreRoot(
    onBreedClick: (String) -> Unit,
    viewModel: ExploreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExploreScreen(state = state, onAction = viewModel::onAction, onBreedClick = onBreedClick)
}

@Composable
fun ExploreScreen(
    state: ExploreUiState,
    onAction: (ExploreAction) -> Unit,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchField(
            query = state.query,
            onQueryChange = { onAction(ExploreAction.QueryChange(it)) },
            placeholder = stringResource(Res.string.explore_search_placeholder),
            enabled = state.isSearchEnabled,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (state.isSearchEnabled) {
            ExploreFilterBar(
                filters = state.filters,
                availableFilters = state.availableFilters,
                groupOptions = state.groupOptions,
                onFiltersChange = { onAction(ExploreAction.FiltersChange(it)) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        when (val content = state.content) {
            ExploreContent.Pending -> {
                Box(Modifier.fillMaxSize())
            }

            ExploreContent.Loading -> {
                LoadingState(
                    title = stringResource(Res.string.breeds_loading_title),
                    body = stringResource(Res.string.breeds_loading_body),
                )
            }

            is ExploreContent.Error -> {
                ErrorState(
                    title = stringResource(Res.string.breeds_error_title),
                    message = stringResource(content.message),
                    retryLabel = stringResource(Res.string.breeds_retry),
                    onRetry = { onAction(ExploreAction.Retry) },
                )
            }

            is ExploreContent.NoMatches -> {
                CenteredColumn {
                    Text(
                        text = noMatchesMessage(content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    HiddenBreedsNote(content.hiddenForMissingInfo)
                }
            }

            is ExploreContent.Results -> {
                if (content.breeds.isEmpty()) {
                    MessageContent(stringResource(Res.string.breeds_empty))
                } else {
                    BreedList(
                        breeds = content.breeds,
                        hiddenForMissingInfo = content.hiddenForMissingInfo,
                        onBreedClick = onBreedClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun noMatchesMessage(content: ExploreContent.NoMatches): String =
    when {
        !content.hasFilters -> stringResource(Res.string.explore_no_matches, content.query)
        content.query.isBlank() -> stringResource(Res.string.explore_no_filter_matches)
        else -> stringResource(Res.string.explore_no_search_filter_matches, content.query)
    }

@Composable
private fun MessageContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    CenteredColumn(modifier) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

@Composable
private fun BreedList(
    breeds: List<BreedListItem>,
    hiddenForMissingInfo: Map<BreedInfo, Int>,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (hiddenForMissingInfo.isNotEmpty()) {
            item(key = HIDDEN_NOTE_KEY) {
                HiddenBreedsNote(hiddenForMissingInfo, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }
        items(breeds, key = { it.id }) { breed -> BreedRow(breed = breed, onClick = { onBreedClick(breed.id) }) }
    }
}

@Composable
private fun BreedRow(
    breed: BreedListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            DogImage(
                urls = breed.imageUrls,
                modifier =
                    Modifier
                        .navSharedElement(breedImageSharedKey(breed.id))
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
            )
            if (breed.isFavourite) {
                FavouriteIndicator(modifier = Modifier.align(Alignment.TopStart).offset(x = (-4).dp, y = (-4).dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = breed.name, style = MaterialTheme.typography.titleMedium)
            breed.subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FavouriteIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_favorite_filled),
            contentDescription = stringResource(Res.string.explore_favourite_indicator),
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(12.dp),
        )
    }
}
