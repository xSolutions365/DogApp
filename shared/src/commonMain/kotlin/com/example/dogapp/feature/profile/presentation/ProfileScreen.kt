package com.example.dogapp.feature.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.animation.breedImageSharedKey
import com.example.dogapp.core.presentation.animation.navSharedElement
import com.example.dogapp.core.presentation.components.DogImage
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.destination_profile
import dogapp.shared.generated.resources.profile_favourite_remove
import dogapp.shared.generated.resources.profile_favourite_remove_description
import dogapp.shared.generated.resources.profile_favourite_unavailable
import dogapp.shared.generated.resources.profile_favourites
import dogapp.shared.generated.resources.profile_favourites_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    onBreedClick: (String) -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(state = state, onAction = viewModel::onAction, onBreedClick = onBreedClick)
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "title") {
            Text(
                text = stringResource(Res.string.destination_profile),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).semantics { heading() },
            )
        }
        item(key = "favourites-title") {
            Text(
                text = stringResource(Res.string.profile_favourites),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp).semantics { heading() },
            )
        }
        val favourites = state.favourites
        if (favourites != null && favourites.isEmpty()) {
            item(key = "favourites-empty") {
                Text(
                    text = stringResource(Res.string.profile_favourites_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
        items(favourites.orEmpty(), key = { it.breedId }) { favourite ->
            when (favourite) {
                is FavouriteListItem.Available -> {
                    AvailableFavouriteRow(favourite, onClick = { onBreedClick(favourite.breedId) })
                }

                is FavouriteListItem.Unavailable -> {
                    UnavailableFavouriteRow(favourite, onRemove = { onAction(ProfileAction.RemoveFavourite(favourite.breedId)) })
                }
            }
        }
    }
}

@Composable
private fun AvailableFavouriteRow(
    favourite: FavouriteListItem.Available,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FavouriteRow(
        name = favourite.name,
        subtitle = favourite.subtitle,
        image = {
            DogImage(
                url = favourite.imageUrl,
                modifier = Modifier.navSharedElement(breedImageSharedKey(favourite.breedId)).size(56.dp).clip(RoundedCornerShape(12.dp)),
            )
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun UnavailableFavouriteRow(
    favourite: FavouriteListItem.Unavailable,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val removeDescription = stringResource(Res.string.profile_favourite_remove_description, favourite.name)
    FavouriteRow(
        name = favourite.name,
        subtitle = stringResource(Res.string.profile_favourite_unavailable),
        image = { DogImage(url = null, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))) },
        trailing = {
            TextButton(onClick = onRemove, modifier = Modifier.semantics { contentDescription = removeDescription }) {
                Text(stringResource(Res.string.profile_favourite_remove))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun FavouriteRow(
    name: String,
    subtitle: String?,
    image: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        image()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing()
    }
}
