package com.example.dogapp.feature.donate.presentation

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.components.platformDisclosureIcon
import com.example.dogapp.feature.donate.domain.Charity
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.donate_charities_title
import dogapp.shared.generated.resources.donate_mock_notice
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CharitiesRoot(
    onCharityClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CharitiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CharitiesScreen(state = state, onCharityClick = onCharityClick, onBack = onBack)
}

@Composable
fun CharitiesScreen(
    state: CharitiesUiState,
    onCharityClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "title") { DonateTopBar(title = stringResource(Res.string.donate_charities_title), onBack = onBack) }
        item(key = "notice") {
            Text(
                text = stringResource(Res.string.donate_mock_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        items(state.charities, key = { "charity-${it.id}" }) { charity ->
            CharityRow(charity = charity, onClick = { onCharityClick(charity.id) })
        }
    }
}

@Composable
private fun CharityRow(
    charity: Charity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = charity.name, style = MaterialTheme.typography.titleMedium)
            Text(text = charity.tagline, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = charity.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(platformDisclosureIcon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}
