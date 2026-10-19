package com.example.dogapp.feature.wallet.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.ObserveAsEvents
import com.example.dogapp.core.presentation.components.BackTopBar
import com.example.dogapp.feature.wallet.domain.CardFieldError
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_add_card
import dogapp.shared.generated.resources.wallet_add_card_add
import dogapp.shared.generated.resources.wallet_add_card_cancel
import dogapp.shared.generated.resources.wallet_add_card_demo_notice
import dogapp.shared.generated.resources.wallet_add_card_ios
import dogapp.shared.generated.resources.wallet_add_card_save
import dogapp.shared.generated.resources.wallet_card_error_expired
import dogapp.shared.generated.resources.wallet_card_error_expiry
import dogapp.shared.generated.resources.wallet_card_error_number
import dogapp.shared.generated.resources.wallet_card_error_security_code
import dogapp.shared.generated.resources.wallet_card_expiry
import dogapp.shared.generated.resources.wallet_card_number
import dogapp.shared.generated.resources.wallet_card_security_code
import dogapp.shared.generated.resources.wallet_hidden_while_recorded
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddCardRoot(
    presentation: AddCardPresentation,
    onDismiss: () -> Unit,
    viewModel: AddCardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SecureScreenEffect()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AddCardEvent.Close -> onDismiss()
        }
    }
    AddCardScreen(
        state = state,
        presentation = presentation,
        isScreenCaptured = rememberIsScreenCaptured(),
        onAction = viewModel::onAction,
        onDismiss = onDismiss,
    )
}

@Composable
fun AddCardScreen(
    state: AddCardUiState,
    presentation: AddCardPresentation,
    isScreenCaptured: Boolean,
    onAction: (AddCardAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (presentation) {
            AddCardPresentation.Screen -> {
                BackTopBar(title = stringResource(Res.string.wallet_add_card), onBack = onDismiss)
            }

            AddCardPresentation.Sheet -> {
                SheetBar(
                    canAdd = !state.isSaving && !isScreenCaptured,
                    onCancel = onDismiss,
                    onAdd = { onAction(AddCardAction.Save) },
                )
            }
        }
        if (isScreenCaptured) {
            Text(
                text = stringResource(Res.string.wallet_hidden_while_recorded),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp).semantics { heading() },
            )
            return@Column
        }
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.wallet_add_card_demo_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CardField(
                value = state.number,
                onValueChange = { onAction(AddCardAction.NumberChange(it)) },
                label = stringResource(Res.string.wallet_card_number),
                contentType = ContentType.CreditCardNumber,
                keyboardType = KeyboardType.Number,
                error = state.errors.numberError(),
                supportingText = state.brand?.let { stringResource(it.displayName) },
            )
            CardField(
                value = state.expiry,
                onValueChange = { onAction(AddCardAction.ExpiryChange(it)) },
                label = stringResource(Res.string.wallet_card_expiry),
                contentType = ContentType.CreditCardExpirationDate,
                keyboardType = KeyboardType.Number,
                error = state.errors.expiryError(),
            )
            CardField(
                value = state.securityCode,
                onValueChange = { onAction(AddCardAction.SecurityCodeChange(it)) },
                label = stringResource(Res.string.wallet_card_security_code),
                contentType = ContentType.CreditCardSecurityCode,
                keyboardType = KeyboardType.NumberPassword,
                error = state.errors.securityCodeError(),
                visualTransformation = PasswordVisualTransformation(),
                imeAction = ImeAction.Done,
            )
            if (presentation == AddCardPresentation.Screen) {
                Button(
                    onClick = { onAction(AddCardAction.Save) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(stringResource(Res.string.wallet_add_card_save))
                }
            }
        }
    }
}

@Composable
private fun SheetBar(
    canAdd: Boolean,
    onCancel: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onCancel) {
            Text(stringResource(Res.string.wallet_add_card_cancel), style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = stringResource(Res.string.wallet_add_card_ios),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        TextButton(onClick = onAdd, enabled = canAdd) {
            Text(
                stringResource(Res.string.wallet_add_card_add),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CardField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    contentType: ContentType,
    keyboardType: KeyboardType,
    error: String?,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: supportingText)?.let { text -> { Text(text) } },
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = modifier.fillMaxWidth().semantics { this.contentType = contentType },
    )
}

@Composable
private fun Set<CardFieldError>.numberError(): String? =
    if (CardFieldError.InvalidNumber in this) stringResource(Res.string.wallet_card_error_number) else null

@Composable
private fun Set<CardFieldError>.expiryError(): String? =
    when {
        CardFieldError.ExpiredCard in this -> stringResource(Res.string.wallet_card_error_expired)
        CardFieldError.InvalidExpiry in this -> stringResource(Res.string.wallet_card_error_expiry)
        else -> null
    }

@Composable
private fun Set<CardFieldError>.securityCodeError(): String? =
    if (CardFieldError.InvalidSecurityCode in this) stringResource(Res.string.wallet_card_error_security_code) else null
