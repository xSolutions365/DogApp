package com.example.dogapp.app.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.example.dogapp.app.navigation.NavigationStyle
import com.example.dogapp.core.presentation.theme.DogAppTheme
import com.example.dogapp.feature.identify.presentation.IdentifyPresentation
import com.example.dogapp.feature.identify.presentation.IdentifyRoot
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.sheetPresentationController

@Composable
actual fun rememberIdentifyLauncher(
    navigationStyle: NavigationStyle,
    navigateToIdentify: () -> Unit,
    onBreedClick: (String) -> Unit,
): () -> Unit {
    val presenter = LocalUIViewController.current
    val currentNavigateToIdentify by rememberUpdatedState(navigateToIdentify)
    val currentOnBreedClick by rememberUpdatedState(onBreedClick)
    return remember(navigationStyle, presenter) {
        when (navigationStyle) {
            NavigationStyle.Material -> {
                { currentNavigateToIdentify() }
            }

            NavigationStyle.Cupertino -> {
                { presentIdentifySheet(presenter) { breedId -> currentOnBreedClick(breedId) } }
            }
        }
    }
}

private fun presentIdentifySheet(
    presenter: UIViewController,
    onBreedClick: (String) -> Unit,
) {
    lateinit var sheet: UIViewController
    sheet =
        ComposeUIViewController {
            DogAppTheme {
                IdentifyRoot(
                    presentation = IdentifyPresentation.Sheet,
                    onBreedClick = { breedId -> sheet.dismissViewControllerAnimated(true) { onBreedClick(breedId) } },
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
