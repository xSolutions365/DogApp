package com.example.dogapp.app.shell

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.PaidWith
import com.example.dogapp.feature.donate.presentation.DonatePresentation
import com.example.dogapp.feature.donate.presentation.DonateSheetRoot
import com.example.dogapp.feature.donate.presentation.displayText
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.donate_thank_you
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

@Composable
expect fun rememberDonateLauncher(
    navigationStyle: NavigationStyle,
    snackbarHostState: SnackbarHostState,
    onOpenWallet: () -> Unit,
): (String) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMaterialDonateSheet(
    snackbarHostState: SnackbarHostState,
    onOpenWallet: () -> Unit,
    onGooglePayDonation: (Donation) -> Unit,
): (String) -> Unit {
    var charityId by rememberSaveable { mutableStateOf<String?>(null) }
    val currentOnOpenWallet by rememberUpdatedState(onOpenWallet)
    val currentOnGooglePayDonation by rememberUpdatedState(onGooglePayDonation)
    val scope = rememberCoroutineScope()
    charityId?.let { id ->
        val sheetOwner = remember(id) { DonateSheetViewModelStoreOwner() }
        DisposableEffect(sheetOwner) { onDispose { sheetOwner.viewModelStore.clear() } }
        ModalBottomSheet(
            onDismissRequest = { charityId = null },
            sheetState =
                rememberBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
                ),
        ) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides sheetOwner) {
                DonateSheetRoot(
                    charityId = id,
                    presentation = DonatePresentation.BottomSheet,
                    onOpenWallet = {
                        charityId = null
                        currentOnOpenWallet()
                    },
                    onDismiss = { charityId = null },
                    onDonationComplete = { donation ->
                        charityId = null
                        when (donation.paidWith) {
                            is PaidWith.GooglePay -> {
                                currentOnGooglePayDonation(donation)
                            }

                            is PaidWith.Card -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        getString(Res.string.donate_thank_you, donation.amount.displayText(), donation.charityName),
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }
    return remember { { id -> charityId = id } }
}

private class DonateSheetViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}
