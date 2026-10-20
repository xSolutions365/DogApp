package com.example.dogapp.feature.donate.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.presentation.ObserveAsEvents
import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentCard
import com.example.dogapp.feature.donate.domain.PaymentError
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.donate_action
import dogapp.shared.generated.resources.donate_add_card
import dogapp.shared.generated.resources.donate_amount_error
import dogapp.shared.generated.resources.donate_button
import dogapp.shared.generated.resources.donate_cancel
import dogapp.shared.generated.resources.donate_card_label
import dogapp.shared.generated.resources.donate_done
import dogapp.shared.generated.resources.donate_error_authentication
import dogapp.shared.generated.resources.donate_error_card_missing
import dogapp.shared.generated.resources.donate_error_declined
import dogapp.shared.generated.resources.donate_error_google_pay
import dogapp.shared.generated.resources.donate_error_wallet
import dogapp.shared.generated.resources.donate_other_amount
import dogapp.shared.generated.resources.donate_other_amount_hint
import dogapp.shared.generated.resources.donate_pay_with
import dogapp.shared.generated.resources.donate_sheet_title
import dogapp.shared.generated.resources.donate_thank_you
import dogapp.shared.generated.resources.donate_thank_you_title
import dogapp.shared.generated.resources.donate_unlock_wallet
import dogapp.shared.generated.resources.ic_check
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DonateSheetRoot(
    charityId: String,
    presentation: DonatePresentation,
    onOpenWallet: () -> Unit,
    onDismiss: () -> Unit,
    onDonationComplete: (Donation) -> Unit,
    viewModel: DonateViewModel = koinViewModel { parametersOf(charityId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val googlePay =
        rememberGooglePayLauncher { outcome ->
            when (outcome) {
                is Outcome.Success -> viewModel.onAction(DonateAction.DonateWithGooglePay(outcome.data))
                is Outcome.Failure -> viewModel.onAction(DonateAction.GooglePayFailed(outcome.error))
            }
        }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DonateEvent.Donated -> onDonationComplete(event.donation)
            is DonateEvent.LaunchGooglePay -> googlePay?.launch(event.amount)
        }
    }
    DonateSheet(
        state = state,
        presentation = presentation,
        googlePay = googlePay,
        onAction = viewModel::onAction,
        onOpenWallet = onOpenWallet,
        onDismiss = onDismiss,
    )
}

@Composable
fun DonateSheet(
    state: DonateUiState,
    presentation: DonatePresentation,
    googlePay: GooglePayLauncher?,
    onAction: (DonateAction) -> Unit,
    onOpenWallet: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completed = state.completed
    if (presentation == DonatePresentation.Sheet && completed != null) {
        ThankYou(donation = completed, onDone = onDismiss, modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        when (presentation) {
            DonatePresentation.BottomSheet -> {
                Text(
                    text = stringResource(Res.string.donate_sheet_title, state.charityName),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).semantics { heading() },
                )
            }

            DonatePresentation.Sheet -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.donate_cancel), style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = stringResource(Res.string.donate_action),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).semantics { heading() },
                    )
                    Spacer(Modifier.width(88.dp))
                }
                Text(
                    text = state.charityName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
        AmountPicker(state = state, presentation = presentation, onAction = onAction)
        Text(
            text = stringResource(Res.string.donate_pay_with),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp).semantics { heading() },
        )
        CardPicker(state = state, presentation = presentation, onAction = onAction, onOpenWallet = onOpenWallet)
        state.error?.message()?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        val amount = state.amount
        if (!state.cards.isNullOrEmpty()) {
            Button(
                onClick = { onAction(DonateAction.DonateWithCard) },
                enabled = !state.isDonating,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    if (amount !=
                        null
                    ) {
                        stringResource(Res.string.donate_button, amount.displayText())
                    } else {
                        stringResource(Res.string.donate_action)
                    },
                )
            }
        }
        googlePay?.PayButton(
            onClick = { onAction(DonateAction.StartGooglePay) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).height(48.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountPicker(
    state: DonateUiState,
    presentation: DonatePresentation,
    onAction: (DonateAction) -> Unit,
) {
    Column {
        val isOther = state.selectedPreset == null
        when (presentation) {
            DonatePresentation.BottomSheet -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    DonationAmount.presets.forEach { preset ->
                        FilterChip(
                            selected = state.selectedPreset == preset,
                            onClick = { onAction(DonateAction.SelectPreset(preset)) },
                            label = { Text(preset.displayText()) },
                        )
                    }
                    FilterChip(
                        selected = isOther,
                        onClick = { onAction(DonateAction.SelectOtherAmount) },
                        label = { Text(stringResource(Res.string.donate_other_amount)) },
                    )
                }
            }

            DonatePresentation.Sheet -> {
                DonationAmount.presets.forEach { preset ->
                    CheckRow(
                        text = preset.displayText(),
                        selected = state.selectedPreset == preset,
                        onClick = { onAction(DonateAction.SelectPreset(preset)) },
                    )
                }
                CheckRow(
                    text = stringResource(Res.string.donate_other_amount),
                    selected = isOther,
                    onClick = { onAction(DonateAction.SelectOtherAmount) },
                )
            }
        }
        if (isOther) {
            OutlinedTextField(
                value = state.otherAmount,
                onValueChange = { onAction(DonateAction.OtherAmountChange(it)) },
                label = { Text(stringResource(Res.string.donate_other_amount)) },
                isError = state.showAmountError,
                supportingText = {
                    Text(stringResource(if (state.showAmountError) Res.string.donate_amount_error else Res.string.donate_other_amount_hint))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun CardPicker(
    state: DonateUiState,
    presentation: DonatePresentation,
    onAction: (DonateAction) -> Unit,
    onOpenWallet: () -> Unit,
) {
    val cards = state.cards
    when {
        cards == null -> {
            OutlinedButton(
                onClick = { onAction(DonateAction.UnlockWallet) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(stringResource(Res.string.donate_unlock_wallet))
            }
        }

        cards.isEmpty() -> {
            TextButton(onClick = onOpenWallet, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(stringResource(Res.string.donate_add_card))
            }
        }

        else -> {
            cards.forEach { card ->
                val selected = card.id == state.selectedCardId
                when (presentation) {
                    DonatePresentation.BottomSheet -> {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = selected, role = Role.RadioButton) { onAction(DonateAction.SelectCard(card.id)) }
                                    .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Text(text = card.label(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    DonatePresentation.Sheet -> {
                        CheckRow(text = card.label(), selected = selected, onClick = { onAction(DonateAction.SelectCard(card.id)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                painterResource(Res.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ThankYou(
    donation: Donation,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.donate_thank_you_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(Res.string.donate_thank_you, donation.amount.displayText(), donation.charityName),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onDone) {
            Text(stringResource(Res.string.donate_done), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PaymentCard.label(): String = stringResource(Res.string.donate_card_label, brandName, last4)

@Composable
private fun PaymentError.message(): String? =
    when (this) {
        PaymentError.Declined -> stringResource(Res.string.donate_error_declined)
        PaymentError.AuthenticationFailed -> stringResource(Res.string.donate_error_authentication)
        PaymentError.WalletUnavailable -> stringResource(Res.string.donate_error_wallet)
        PaymentError.CardNotFound -> stringResource(Res.string.donate_error_card_missing)
        PaymentError.GooglePayFailed -> stringResource(Res.string.donate_error_google_pay)
        PaymentError.Cancelled -> null
    }
