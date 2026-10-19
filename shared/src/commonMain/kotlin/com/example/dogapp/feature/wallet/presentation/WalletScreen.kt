package com.example.dogapp.feature.wallet.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.components.BackTopBar
import com.example.dogapp.core.presentation.components.platformLockIcon
import com.example.dogapp.core.presentation.toDisplayText
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_created
import dogapp.shared.generated.resources.wallet_hidden_while_recorded
import dogapp.shared.generated.resources.wallet_locked_message
import dogapp.shared.generated.resources.wallet_locked_message_ios
import dogapp.shared.generated.resources.wallet_locked_title
import dogapp.shared.generated.resources.wallet_locked_title_ios
import dogapp.shared.generated.resources.wallet_open_settings
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WalletRoot(
    layout: WalletLayout,
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = layout.copy()
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBar(title = stringResource(Res.string.wallet_title), onBack = onBack)
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
                Text(
                    text = stringResource(Res.string.wallet_created, state.createdOn.toDisplayText()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
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
