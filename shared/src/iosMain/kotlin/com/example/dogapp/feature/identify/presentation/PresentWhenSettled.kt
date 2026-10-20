package com.example.dogapp.feature.identify.presentation

import platform.UIKit.UIViewController
import platform.UIKit.UIViewControllerTransitionCoordinatorContextProtocol
import platform.UIKit.transitionCoordinator

internal fun UIViewController.presentWhenSettled(controller: UIViewController) {
    val coordinator = transitionCoordinator
    if (coordinator == null) {
        presentViewController(controller, animated = true, completion = null)
    } else {
        coordinator.animateAlongsideTransition(animation = null) { _: UIViewControllerTransitionCoordinatorContextProtocol? ->
            presentViewController(controller, animated = true, completion = null)
        }
    }
}
