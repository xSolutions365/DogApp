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
import androidx.compose.material3.Icon
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
import com.example.dogapp.core.presentation.components.platformDisclosureIcon
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.breed_unavailable
import dogapp.shared.generated.resources.destination_profile
import dogapp.shared.generated.resources.profile_donations
import dogapp.shared.generated.resources.profile_draws
import dogapp.shared.generated.resources.profile_draws_empty
import dogapp.shared.generated.resources.profile_favourite_remove
import dogapp.shared.generated.resources.profile_favourite_remove_description
import dogapp.shared.generated.resources.profile_favourites
import dogapp.shared.generated.resources.profile_favourites_empty
import dogapp.shared.generated.resources.profile_feed_me
import dogapp.shared.generated.resources.profile_support_charities
import dogapp.shared.generated.resources.profile_wallet
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    onBreedClick: (String) -> Unit,
    onSupportCharitiesClick: () -> Unit,
    onDonationsClick: () -> Unit,
    onWalletClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onBreedClick = onBreedClick,
        onSupportCharitiesClick = onSupportCharitiesClick,
        onDonationsClick = onDonationsClick,
        onWalletClick = onWalletClick,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBreedClick: (String) -> Unit,
    onSupportCharitiesClick: () -> Unit,
    onDonationsClick: () -> Unit,
    onWalletClick: () -> Unit,
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
        items(favourites.orEmpty(), key = { "favourite-${it.breedId}" }) { favourite ->
            when (favourite) {
                is FavouriteListItem.Available -> {
                    AvailableFavouriteRow(favourite, onClick = { onBreedClick(favourite.breedId) })
                }

                is FavouriteListItem.Unavailable -> {
                    UnavailableFavouriteRow(favourite, onRemove = { onAction(ProfileAction.RemoveFavourite(favourite.breedId)) })
                }
            }
        }
        item(key = "draws-title") {
            Text(
                text = stringResource(Res.string.profile_draws),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp).semantics { heading() },
            )
        }
        val draws = state.draws
        if (draws != null && draws.isEmpty()) {
            item(key = "draws-empty") {
                Text(
                    text = stringResource(Res.string.profile_draws_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
        items(draws.orEmpty(), key = { "draw-${it.day}" }) { draw ->
            DrawHistoryRow(draw = draw, onClick = { onBreedClick(draw.breedId) })
        }
        item(key = "feed-me-title") {
            Text(
                text = stringResource(Res.string.profile_feed_me),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp).semantics { heading() },
            )
        }
        item(key = "support-charities") {
            FeedMeRow(text = stringResource(Res.string.profile_support_charities), onClick = onSupportCharitiesClick)
        }
        item(key = "donations") {
            FeedMeRow(text = stringResource(Res.string.profile_donations), onClick = onDonationsClick)
        }
        item(key = "wallet") {
            FeedMeRow(text = stringResource(Res.string.profile_wallet), onClick = onWalletClick)
        }
    }
}

@Composable
private fun FeedMeRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(platformDisclosureIcon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun DrawHistoryRow(
    draw: DrawHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FavouriteRow(
        name = draw.name,
        subtitle = if (draw.isAvailable) draw.date else "${draw.date} · ${stringResource(Res.string.breed_unavailable)}",
        image = { DogImage(urls = draw.imageUrls, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))) },
        modifier = if (draw.isAvailable) modifier.clickable(onClick = onClick) else modifier,
    )
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
                urls = favourite.imageUrls,
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
        subtitle = stringResource(Res.string.breed_unavailable),
        image = { DogImage(urls = emptyList(), modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))) },
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
