package com.example.dogapp.feature.home.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.home_choose_photo_cupertino
import dogapp.shared.generated.resources.home_identify_action
import dogapp.shared.generated.resources.home_take_photo_cupertino
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.stringResource
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIImage
import platform.UIKit.UIMenu

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun IdentifyMenuButton(
    onTakePhotoClick: () -> Unit,
    onChoosePhotoClick: () -> Unit,
    modifier: Modifier,
) {
    val title = stringResource(Res.string.home_identify_action)
    val takePhoto = stringResource(Res.string.home_take_photo_cupertino)
    val choosePhoto = stringResource(Res.string.home_choose_photo_cupertino)
    val tint = MaterialTheme.colorScheme.primary.toUIColor()
    val background = MaterialTheme.colorScheme.background.toUIColor()
    val currentOnTakePhotoClick by rememberUpdatedState(onTakePhotoClick)
    val currentOnChoosePhotoClick by rememberUpdatedState(onChoosePhotoClick)
    UIKitView(
        factory = {
            UIButton.buttonWithType(UIButtonTypeSystem).apply {
                setTitle(title, forState = UIControlStateNormal)
                titleLabel?.font = UIFont.systemFontOfSize(BUTTON_FONT_SIZE, weight = UIFontWeightSemibold)
                showsMenuAsPrimaryAction = true
                menu =
                    UIMenu.menuWithTitle(
                        title = "",
                        children =
                            listOf(
                                UIAction.actionWithTitle(takePhoto, image = UIImage.systemImageNamed("camera"), identifier = null) { _ ->
                                    currentOnTakePhotoClick()
                                },
                                UIAction.actionWithTitle(
                                    choosePhoto,
                                    image = UIImage.systemImageNamed("photo.on.rectangle"),
                                    identifier = null,
                                ) { _ -> currentOnChoosePhotoClick() },
                            ),
                    )
            }
        },
        update = { button ->
            button.tintColor = tint
            button.backgroundColor = background
        },
        modifier =
            modifier.size(width = 96.dp, height = 44.dp).semantics {
                contentDescription = title
                role = Role.Button
            },
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true),
    )
}

private fun Color.toUIColor(): UIColor =
    UIColor.colorWithRed(red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = alpha.toDouble())

private const val BUTTON_FONT_SIZE = 17.0
