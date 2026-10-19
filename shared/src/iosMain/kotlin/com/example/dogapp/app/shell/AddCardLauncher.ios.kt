package com.example.dogapp.app.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.wallet.presentation.AddCardPresentation
import com.example.dogapp.feature.wallet.presentation.AddCardRoot
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.sheetPresentationController

@Composable
actual fun rememberAddCardLauncher(
    navigationStyle: NavigationStyle,
    navigateToAddCard: () -> Unit,
): () -> Unit {
    val presenter = LocalUIViewController.current
    val currentNavigateToAddCard by rememberUpdatedState(navigateToAddCard)
    return remember(navigationStyle, presenter) {
        when (navigationStyle) {
            NavigationStyle.Material -> {
                { currentNavigateToAddCard() }
            }

            NavigationStyle.Cupertino -> {
                { presentAddCardSheet(presenter) }
            }
        }
    }
}

private fun presentAddCardSheet(presenter: UIViewController) {
    lateinit var sheet: UIViewController
    sheet =
        ComposeUIViewController {
            DogAppTheme {
                AddCardRoot(
                    presentation = AddCardPresentation.Sheet,
                    onDismiss = { sheet.dismissViewControllerAnimated(true, completion = null) },
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
