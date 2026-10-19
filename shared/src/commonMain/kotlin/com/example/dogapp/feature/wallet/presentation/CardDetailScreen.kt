package com.example.dogapp.feature.wallet.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.ObserveAsEvents
import com.example.dogapp.core.presentation.components.BackTopBar
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_remove_card
import dogapp.shared.generated.resources.wallet_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CardDetailRoot(
    cardId: String,
    onBack: () -> Unit,
    viewModel: CardDetailViewModel = koinViewModel { parametersOf(cardId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SecureScreenEffect()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CardDetailEvent.Close -> onBack()
        }
    }
    CardDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun CardDetailScreen(
    state: CardDetailUiState,
    onAction: (CardDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.card
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBar(title = card?.label() ?: stringResource(Res.string.wallet_title), onBack = onBack)
        if (card == null) return@Column
        Text(
            text = card.expiry.expiresText(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        OutlinedButton(
            onClick = { onAction(CardDetailAction.Remove) },
            enabled = !state.isRemoving,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(stringResource(Res.string.wallet_remove_card))
        }
    }
}
