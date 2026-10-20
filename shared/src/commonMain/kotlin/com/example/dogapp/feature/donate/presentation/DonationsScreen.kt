package com.example.dogapp.feature.donate.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.presentation.components.BackTopBar
import com.example.dogapp.feature.donate.domain.PaidWith
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.donate_card_label
import dogapp.shared.generated.resources.donations_empty
import dogapp.shared.generated.resources.donations_notice
import dogapp.shared.generated.resources.donations_paid_with
import dogapp.shared.generated.resources.donations_paid_with_google_pay
import dogapp.shared.generated.resources.donations_paid_with_google_pay_card
import dogapp.shared.generated.resources.donations_see_charities
import dogapp.shared.generated.resources.donations_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DonationsRoot(
    onSeeCharitiesClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: DonationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DonationsScreen(state = state, onSeeCharitiesClick = onSeeCharitiesClick, onBack = onBack)
}

@Composable
fun DonationsScreen(
    state: DonationsUiState,
    onSeeCharitiesClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val donations = state.donations
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "title") { BackTopBar(title = stringResource(Res.string.donations_title), onBack = onBack) }
        item(key = "notice") {
            Text(
                text = stringResource(Res.string.donations_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (donations?.isEmpty() == true) {
            item(key = "empty") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = stringResource(Res.string.donations_empty), style = MaterialTheme.typography.bodyLarge)
                    OutlinedButton(onClick = onSeeCharitiesClick) {
                        Text(stringResource(Res.string.donations_see_charities))
                    }
                }
            }
        }
        items(donations.orEmpty(), key = { "donation-${it.id}" }) { donation ->
            DonationRow(donation = donation)
        }
    }
}

@Composable
private fun DonationRow(
    donation: DonationListItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = donation.charityName, style = MaterialTheme.typography.titleMedium)
            Text(text = donation.paidWith.text(), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = donation.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = donation.amount.displayText(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun PaidWith.text(): String =
    when (this) {
        is PaidWith.Card -> {
            stringResource(Res.string.donations_paid_with, stringResource(Res.string.donate_card_label, brandName, last4))
        }

        is PaidWith.GooglePay -> {
            cardDescription?.let { stringResource(Res.string.donations_paid_with_google_pay_card, it) }
                ?: stringResource(Res.string.donations_paid_with_google_pay)
        }
    }
