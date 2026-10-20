package com.example.dogapp.feature.identify.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readValue
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIColor
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberPhotoCapture(
    onPhotoCapture: (ByteArray) -> Unit,
    onChoosePhoto: () -> Unit,
): () -> Unit {
    val presenter = LocalUIViewController.current
    val currentOnPhotoCapture by rememberUpdatedState(onPhotoCapture)
    val currentOnChoosePhoto by rememberUpdatedState(onChoosePhoto)
    return remember(presenter) {
        {
            presentCamera(
                presenter = presenter,
                onPhotoCapture = { photo -> currentOnPhotoCapture(photo) },
                onChoosePhoto = { currentOnChoosePhoto() },
            )
        }
    }
}

private fun presentCamera(
    presenter: UIViewController,
    onPhotoCapture: (ByteArray) -> Unit,
    onChoosePhoto: () -> Unit,
) {
    lateinit var camera: UIViewController
    camera =
        ComposeUIViewController {
            DogAppTheme {
                IosCamera(
                    onPhotoCapture = { photo -> camera.dismissViewControllerAnimated(true) { onPhotoCapture(photo) } },
                    onChoosePhoto = { camera.dismissViewControllerAnimated(true) { onChoosePhoto() } },
                    onClose = { camera.dismissViewControllerAnimated(true, completion = null) },
                )
            }
        }
    camera.modalPresentationStyle = UIModalPresentationFullScreen
    presenter.presentWhenSettled(camera)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun IosCamera(
    onPhotoCapture: (ByteArray) -> Unit,
    onChoosePhoto: () -> Unit,
    onClose: () -> Unit,
) {
    val device = remember { AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) }
    var access by remember { mutableStateOf(if (device == null) CameraAccess.Unavailable else currentAccess()) }
    var isCapturing by remember { mutableStateOf(false) }
    val camera = remember(device) { device?.let(::CameraSession) }
    DisposableEffect(Unit) {
        if (device != null && AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusNotDetermined) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { _ ->
                dispatch_async(dispatch_get_main_queue()) { access = currentAccess() }
            }
        }
        onDispose { camera?.stop() }
    }
    CameraScreen(
        access = access,
        isCapturing = isCapturing,
        onShutterClick = {
            isCapturing = true
            camera?.capture { photo ->
                isCapturing = false
                photo?.let(onPhotoCapture)
            }
        },
        onAllowAccess = {},
        onOpenSettings = {
            NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { url ->
                UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
            }
        },
        onChoosePhoto = onChoosePhoto,
        onClose = onClose,
    ) {
        camera?.let { session ->
            UIKitView(
                factory = { session.start() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun currentAccess(): CameraAccess? =
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> CameraAccess.Granted
        AVAuthorizationStatusNotDetermined -> null
        else -> CameraAccess.PermanentlyDenied
    }

@OptIn(ExperimentalForeignApi::class)
private class CameraSession(
    device: AVCaptureDevice,
) {
    private val session = AVCaptureSession()
    private val photoOutput = AVCapturePhotoOutput()
    private var delegate: PhotoDelegate? = null

    init {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        AVCaptureDeviceInput.deviceInputWithDevice(device, error = null)?.let { input ->
            if (session.canAddInput(input)) session.addInput(input)
        }
        if (session.canAddOutput(photoOutput)) session.addOutput(photoOutput)
    }

    fun start(): UIView {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) { session.startRunning() }
        return PreviewView(AVCaptureVideoPreviewLayer(session = session))
    }

    fun stop() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) { session.stopRunning() }
    }

    fun capture(onPhoto: (ByteArray?) -> Unit) {
        val settings = AVCapturePhotoSettings.photoSettingsWithFormat(mapOf<Any?, Any?>(AVVideoCodecKey to AVVideoCodecTypeJPEG))
        val photoDelegate =
            PhotoDelegate { photo ->
                delegate = null
                dispatch_async(dispatch_get_main_queue()) { onPhoto(photo) }
            }
        delegate = photoDelegate
        photoOutput.capturePhotoWithSettings(settings, delegate = photoDelegate)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PreviewView(
    private val previewLayer: AVCaptureVideoPreviewLayer,
) : UIView(frame = CGRectZero.readValue()) {
    init {
        backgroundColor = UIColor.blackColor
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class PhotoDelegate(
    private val onPhoto: (ByteArray?) -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    @ObjCSignatureOverride
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) Logger.w { "Camera capture failed: ${error.localizedDescription}" }
        onPhoto(didFinishProcessingPhoto.fileDataRepresentation()?.toByteArray())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, length) }
    return bytes
}
