package com.example.dogapp.feature.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.components.BackTopBar
import com.example.dogapp.core.presentation.components.platformLockIcon
import com.example.dogapp.feature.wallet.domain.SavedCard
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.ic_add
import dogapp.shared.generated.resources.wallet_add_card
import dogapp.shared.generated.resources.wallet_add_card_ios
import dogapp.shared.generated.resources.wallet_hidden_while_recorded
import dogapp.shared.generated.resources.wallet_locked_message
import dogapp.shared.generated.resources.wallet_locked_message_ios
import dogapp.shared.generated.resources.wallet_locked_title
import dogapp.shared.generated.resources.wallet_locked_title_ios
import dogapp.shared.generated.resources.wallet_no_cards
import dogapp.shared.generated.resources.wallet_open_settings
import dogapp.shared.generated.resources.wallet_remove
import dogapp.shared.generated.resources.wallet_reset_message
import dogapp.shared.generated.resources.wallet_reset_message_ios
import dogapp.shared.generated.resources.wallet_reset_title
import dogapp.shared.generated.resources.wallet_reset_title_ios
import dogapp.shared.generated.resources.wallet_set_up_again
import dogapp.shared.generated.resources.wallet_set_up_again_ios
import dogapp.shared.generated.resources.wallet_title
import dogapp.shared.generated.resources.wallet_unavailable_message
import dogapp.shared.generated.resources.wallet_unavailable_message_ios
import dogapp.shared.generated.resources.wallet_unlock
import dogapp.shared.generated.resources.wallet_unlock_ios
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WalletRoot(
    layout: WalletLayout,
    onAddCardClick: () -> Unit,
    onCardClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: WalletViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SecureScreenEffect()
    LifecycleResumeEffect(viewModel) {
        viewModel.onAction(WalletAction.ScreenResume)
        onPauseOrDispose {}
    }
    WalletScreen(
        state = state,
        layout = layout,
        biometricName = rememberBiometricName(),
        isScreenCaptured = rememberIsScreenCaptured(),
        onAction = viewModel::onAction,
        onOpenSettings = rememberBiometricEnrollLauncher(),
        onAddCardClick = onAddCardClick,
        onCardClick = onCardClick,
        onBack = onBack,
    )
}

@Composable
fun WalletScreen(
    state: WalletUiState,
    layout: WalletLayout,
    biometricName: String,
    isScreenCaptured: Boolean,
    onAction: (WalletAction) -> Unit,
    onOpenSettings: (() -> Unit)?,
    onAddCardClick: () -> Unit,
    onCardClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = layout.copy()
    val canAddCard = state is WalletUiState.Unlocked && !isScreenCaptured
    Box(modifier = modifier.fillMaxSize()) {
        WalletContent(
            state = state,
            layout = layout,
            copy = copy,
            biometricName = biometricName,
            isScreenCaptured = isScreenCaptured,
            canAddCard = canAddCard,
            onAction = onAction,
            onOpenSettings = onOpenSettings,
            onAddCardClick = onAddCardClick,
            onCardClick = onCardClick,
            onBack = onBack,
        )
        if (canAddCard && layout == WalletLayout.Material) {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(Res.string.wallet_add_card)) },
                icon = { Icon(painterResource(Res.drawable.ic_add), contentDescription = stringResource(Res.string.wallet_add_card)) },
                onClick = onAddCardClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }
}

@Composable
private fun WalletContent(
    state: WalletUiState,
    layout: WalletLayout,
    copy: WalletCopy,
    biometricName: String,
    isScreenCaptured: Boolean,
    canAddCard: Boolean,
    onAction: (WalletAction) -> Unit,
    onOpenSettings: (() -> Unit)?,
    onAddCardClick: () -> Unit,
    onCardClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBar(title = stringResource(Res.string.wallet_title), onBack = onBack) {
            if (canAddCard && layout == WalletLayout.Cupertino) {
                TextButton(onClick = onAddCardClick) {
                    Text(stringResource(Res.string.wallet_add_card_ios), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        if (isScreenCaptured) {
            WalletMessage(
                layout = layout,
                title = stringResource(Res.string.wallet_hidden_while_recorded),
                message = null,
                actionLabel = null,
                onActionClick = {},
            )
            return@Column
        }
        when (state) {
            WalletUiState.Locked -> {
                WalletMessage(
                    layout = layout,
                    title = stringResource(copy.lockedTitle),
                    message = stringResource(copy.lockedMessage, biometricName),
                    actionLabel = stringResource(copy.unlock, biometricName),
                    onActionClick = { onAction(WalletAction.Unlock) },
                )
            }

            WalletUiState.KeyInvalidated -> {
                WalletMessage(
                    layout = layout,
                    title = stringResource(copy.resetTitle),
                    message = stringResource(copy.resetMessage, biometricName),
                    actionLabel = stringResource(copy.setUpAgain),
                    onActionClick = { onAction(WalletAction.SetUpAgain) },
                )
            }

            WalletUiState.BiometricUnavailable -> {
                WalletMessage(
                    layout = layout,
                    title = stringResource(copy.unavailableMessage, biometricName),
                    message = null,
                    actionLabel = onOpenSettings?.let { stringResource(Res.string.wallet_open_settings) },
                    onActionClick = { onOpenSettings?.invoke() },
                )
            }

            is WalletUiState.Unlocked -> {
                CardList(
                    cards = state.cards,
                    layout = layout,
                    onCardClick = onCardClick,
                    onRemoveCard = { cardId -> onAction(WalletAction.RemoveCard(cardId)) },
                )
            }
        }
    }
}

@Composable
private fun CardList(
    cards: List<SavedCard>,
    layout: WalletLayout,
    onCardClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) {
        Text(
            text = stringResource(Res.string.wallet_no_cards),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
        items(cards, key = { it.id }) { card ->
            when (layout) {
                WalletLayout.Material -> CardRow(card = card, modifier = Modifier.clickable { onCardClick(card.id) })
                WalletLayout.Cupertino -> SwipeToRemoveCardRow(card = card, onRemove = { onRemoveCard(card.id) })
            }
        }
    }
}

@Composable
private fun SwipeToRemoveCardRow(
    card: SavedCard,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val removeLabel = stringResource(Res.string.wallet_remove)
    val swipeState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    val currentOnRemove by rememberUpdatedState(onRemove)
    val onSwipeDismiss: (SwipeToDismissBoxValue) -> Unit =
        remember(swipeState, scope) {
            { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) currentOnRemove()
                scope.launch { swipeState.reset() }
            }
        }
    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        onDismiss = onSwipeDismiss,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.error).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(text = removeLabel, color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.titleMedium)
            }
        },
        modifier =
            modifier.semantics {
                customActions =
                    listOf(
                        CustomAccessibilityAction(removeLabel) {
                            onRemove()
                            true
                        },
                    )
            },
    ) {
        CardRow(card = card, modifier = Modifier.background(MaterialTheme.colorScheme.surface))
    }
}

@Composable
private fun CardRow(
    card: SavedCard,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = card.label(), style = MaterialTheme.typography.titleMedium)
        Text(
            text = card.expiry.expiresText(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WalletMessage(
    layout: WalletLayout,
    title: String,
    message: String?,
    actionLabel: String?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(platformLockIcon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null) {
            when (layout) {
                WalletLayout.Material -> Button(onClick = onActionClick) { Text(actionLabel) }
                WalletLayout.Cupertino -> TextButton(onClick = onActionClick) { Text(actionLabel) }
            }
        }
    }
}

private class WalletCopy(
    val lockedTitle: StringResource,
    val lockedMessage: StringResource,
    val unlock: StringResource,
    val resetTitle: StringResource,
    val resetMessage: StringResource,
    val setUpAgain: StringResource,
    val unavailableMessage: StringResource,
)

private fun WalletLayout.copy(): WalletCopy =
    when (this) {
        WalletLayout.Material -> {
            WalletCopy(
                lockedTitle = Res.string.wallet_locked_title,
                lockedMessage = Res.string.wallet_locked_message,
                unlock = Res.string.wallet_unlock,
                resetTitle = Res.string.wallet_reset_title,
                resetMessage = Res.string.wallet_reset_message,
                setUpAgain = Res.string.wallet_set_up_again,
                unavailableMessage = Res.string.wallet_unavailable_message,
            )
        }

        WalletLayout.Cupertino -> {
            WalletCopy(
                lockedTitle = Res.string.wallet_locked_title_ios,
                lockedMessage = Res.string.wallet_locked_message_ios,
                unlock = Res.string.wallet_unlock_ios,
                resetTitle = Res.string.wallet_reset_title_ios,
                resetMessage = Res.string.wallet_reset_message_ios,
                setUpAgain = Res.string.wallet_set_up_again_ios,
                unavailableMessage = Res.string.wallet_unavailable_message_ios,
            )
        }
    }
