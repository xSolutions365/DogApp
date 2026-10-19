package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UISceneCaptureStateActive
import platform.UIKit.UISceneDidActivateNotification
import platform.UIKit.UISceneWillDeactivateNotification
import platform.UIKit.UITraitChangeObservableProtocol
import platform.UIKit.UITraitCollection
import platform.UIKit.UITraitEnvironmentProtocol
import platform.UIKit.UITraitSceneCaptureState
import platform.UIKit.UIViewAutoresizingFlexibleBottomMargin
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleLeftMargin
import platform.UIKit.UIViewAutoresizingFlexibleRightMargin
import platform.UIKit.UIViewAutoresizingFlexibleTopMargin
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIVisualEffectView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SecureScreenEffect() {
    val viewController = LocalUIViewController.current
    DisposableEffect(viewController) {
        var overlay: UIVisualEffectView? = null
        val center = NSNotificationCenter.defaultCenter
        val willDeactivate =
            center.addObserverForName(UISceneWillDeactivateNotification, null, NSOperationQueue.mainQueue) { _ ->
                val window = viewController.view.window ?: return@addObserverForName
                if (overlay == null) overlay = privacyOverlay(window.bounds).also { window.addSubview(it) }
            }
        val didActivate =
            center.addObserverForName(UISceneDidActivateNotification, null, NSOperationQueue.mainQueue) { _ ->
                overlay?.removeFromSuperview()
                overlay = null
            }
        onDispose {
            center.removeObserver(willDeactivate)
            center.removeObserver(didActivate)
            overlay?.removeFromSuperview()
        }
    }
}

@Composable
actual fun rememberIsScreenCaptured(): Boolean {
    val viewController = LocalUIViewController.current
    var isCaptured by remember(viewController) {
        mutableStateOf(viewController.traitCollection.sceneCaptureState == UISceneCaptureStateActive)
    }
    DisposableEffect(viewController) {
        val observable = viewController as UITraitChangeObservableProtocol
        val registration =
            observable.registerForTraitChanges(
                listOf(UITraitSceneCaptureState),
            ) { environment: UITraitEnvironmentProtocol?, _: UITraitCollection? ->
                isCaptured = environment?.traitCollection?.sceneCaptureState == UISceneCaptureStateActive
            }
        onDispose { observable.unregisterForTraitChanges(registration) }
    }
    return isCaptured
}

@OptIn(ExperimentalForeignApi::class)
private fun privacyOverlay(bounds: CValue<CGRect>): UIVisualEffectView {
    val overlay = UIVisualEffectView(effect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleSystemMaterial))
    overlay.setFrame(bounds)
    overlay.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
    val icon = UIImageView(image = UIImage.imageNamed(PRIVACY_ICON))
    val (width, height) = bounds.useContents { size.width to size.height }
    icon.setFrame(CGRectMake((width - ICON_SIZE) / 2, (height - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE))
    icon.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
    icon.layer.cornerRadius = ICON_CORNER_RADIUS
    icon.layer.masksToBounds = true
    icon.autoresizingMask =
        UIViewAutoresizingFlexibleLeftMargin or UIViewAutoresizingFlexibleRightMargin or
        UIViewAutoresizingFlexibleTopMargin or UIViewAutoresizingFlexibleBottomMargin
    overlay.contentView.addSubview(icon)
    return overlay
}

private const val PRIVACY_ICON = "PrivacyIcon"
private const val ICON_SIZE = 96.0
private const val ICON_CORNER_RADIUS = 22.0
