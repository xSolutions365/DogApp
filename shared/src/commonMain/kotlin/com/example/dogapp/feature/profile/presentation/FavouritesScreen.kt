package com.example.dogapp.feature.profile.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.components.BackTopBar
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.profile_favourites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavouritesRoot(
    onBreedClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FavouritesScreen(favourites = state.favourites, onAction = viewModel::onAction, onBreedClick = onBreedClick, onBack = onBack)
}

@Composable
fun FavouritesScreen(
    favourites: List<FavouriteListItem>?,
    onAction: (ProfileAction) -> Unit,
    onBreedClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "title") { BackTopBar(title = stringResource(Res.string.profile_favourites), onBack = onBack) }
        if (favourites != null && favourites.isEmpty()) {
            item(key = "favourites-empty") { FavouritesEmptyText() }
        }
        items(favourites.orEmpty(), key = { "favourite-${it.breedId}" }) { favourite ->
            FavouriteItemRow(favourite = favourite, onAction = onAction, onBreedClick = onBreedClick)
        }
    }
}
