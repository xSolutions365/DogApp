package com.example.dogapp.feature.donate.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.components.BackTopBar
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.donate_action
import dogapp.shared.generated.resources.donate_charities_title
import dogapp.shared.generated.resources.donate_charity_about
import dogapp.shared.generated.resources.donate_charity_not_found
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CharityRoot(
    charityId: String,
    onDonateClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CharityViewModel = koinViewModel { parametersOf(charityId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CharityScreen(state = state, onDonateClick = { onDonateClick(charityId) }, onBack = onBack)
}

@Composable
fun CharityScreen(
    state: CharityUiState,
    onDonateClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when (state) {
            CharityUiState.NotFound -> {
                BackTopBar(title = stringResource(Res.string.donate_charities_title), onBack = onBack)
                Text(
                    text = stringResource(Res.string.donate_charity_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            is CharityUiState.Loaded -> {
                val charity = state.charity
                BackTopBar(title = charity.name, onBack = onBack)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = charity.tagline, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = charity.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = onDonateClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(stringResource(Res.string.donate_action))
                }
                Text(
                    text = stringResource(Res.string.donate_charity_about),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp).semantics { heading() },
                )
                Text(
                    text = charity.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
