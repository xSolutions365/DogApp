package com.example.dogapp.app.shell

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.donate.presentation.DonatePresentation
import com.example.dogapp.feature.donate.presentation.DonateSheetRoot
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.sheetPresentationController

@Composable
actual fun rememberDonateLauncher(
    navigationStyle: NavigationStyle,
    snackbarHostState: SnackbarHostState,
    onOpenWallet: () -> Unit,
): (String) -> Unit {
    val materialSheet = rememberMaterialDonateSheet(snackbarHostState, onOpenWallet)
    val presenter = LocalUIViewController.current
    val currentOnOpenWallet by rememberUpdatedState(onOpenWallet)
    return remember(navigationStyle, presenter, materialSheet) {
        when (navigationStyle) {
            NavigationStyle.Material -> materialSheet
            NavigationStyle.Cupertino -> { charityId -> presentDonateSheet(presenter, charityId) { currentOnOpenWallet() } }
        }
    }
}

private fun presentDonateSheet(
    presenter: UIViewController,
    charityId: String,
    onOpenWallet: () -> Unit,
) {
    lateinit var sheet: UIViewController
    sheet =
        ComposeUIViewController {
            DogAppTheme {
                DonateSheetRoot(
                    charityId = charityId,
                    presentation = DonatePresentation.Sheet,
                    onOpenWallet = { sheet.dismissViewControllerAnimated(true) { onOpenWallet() } },
                    onDismiss = { sheet.dismissViewControllerAnimated(true, completion = null) },
                    onDonationComplete = {},
                )
            }
        }
    sheet.modalPresentationStyle = UIModalPresentationPageSheet
    sheet.sheetPresentationController?.apply {
        detents = listOf(UISheetPresentationControllerDetent.largeDetent())
        prefersGrabberVisible = true
    }
    presenter.presentViewController(sheet, animated = true, completion = null)
}
