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
import dogapp.shared.generated.resources.profile_draws
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DrawHistoryRoot(
    onBreedClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DrawHistoryScreen(draws = state.draws, onBreedClick = onBreedClick, onBack = onBack)
}

@Composable
fun DrawHistoryScreen(
    draws: List<DrawHistoryItem>?,
    onBreedClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "title") { BackTopBar(title = stringResource(Res.string.profile_draws), onBack = onBack) }
        if (draws != null && draws.isEmpty()) {
            item(key = "draws-empty") { DrawsEmptyText() }
        }
        items(draws.orEmpty(), key = { "draw-${it.day}" }) { draw ->
            DrawHistoryRow(draw = draw, onClick = { onBreedClick(draw.breedId) })
        }
    }
}
