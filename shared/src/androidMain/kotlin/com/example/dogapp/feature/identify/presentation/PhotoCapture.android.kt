package com.example.dogapp.feature.identify.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

@Composable
actual fun rememberPhotoCapture(
    onPhotoCapture: (ByteArray) -> Unit,
    onChoosePhoto: () -> Unit,
): () -> Unit {
    var isOpen by rememberSaveable { mutableStateOf(false) }
    val currentOnPhotoCapture by rememberUpdatedState(onPhotoCapture)
    val currentOnChoosePhoto by rememberUpdatedState(onChoosePhoto)
    if (isOpen) {
        Dialog(
            onDismissRequest = { isOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            AndroidCamera(
                onPhotoCapture = { photo ->
                    isOpen = false
                    currentOnPhotoCapture(photo)
                },
                onChoosePhoto = {
                    isOpen = false
                    currentOnChoosePhoto()
                },
                onClose = { isOpen = false },
            )
        }
    }
    return remember { { isOpen = true } }
}

@Composable
private fun AndroidCamera(
    onPhotoCapture: (ByteArray) -> Unit,
    onChoosePhoto: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    var hasAsked by rememberSaveable { mutableStateOf(false) }
    var access by remember { mutableStateOf<CameraAccess?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            hasAsked = true
            access = cameraAccess(context, hasAsked, activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, CAMERA) })
        }
    LifecycleResumeEffect(hasAsked) {
        access = cameraAccess(context, hasAsked, activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, CAMERA) })
        if (access == null) permissionLauncher.launch(CAMERA)
        onPauseOrDispose {}
    }
    CameraScreen(
        access = access,
        isCapturing = isCapturing,
        onShutterClick = {
            isCapturing = true
            scope.launch {
                val photo = imageCapture.captureUprightJpeg()
                isCapturing = false
                photo?.let(onPhotoCapture)
            }
        },
        onAllowAccess = { permissionLauncher.launch(CAMERA) },
        onOpenSettings = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
        },
        onChoosePhoto = onChoosePhoto,
        onClose = onClose,
    ) {
        CameraViewfinder(imageCapture = imageCapture, modifier = Modifier.fillMaxSize())
    }
}

private fun cameraAccess(
    context: Context,
    hasAsked: Boolean,
    showRationale: Boolean?,
): CameraAccess? =
    when {
        !context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) -> CameraAccess.Unavailable
        ContextCompat.checkSelfPermission(context, CAMERA) == PackageManager.PERMISSION_GRANTED -> CameraAccess.Granted
        !hasAsked -> null
        showRationale == true -> CameraAccess.Denied
        else -> CameraAccess.PermanentlyDenied
    }

@Composable
private fun CameraViewfinder(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    val preview = remember { Preview.Builder().build() }
    LaunchedEffect(lifecycleOwner) {
        preview.setSurfaceProvider { request -> surfaceRequest = request }
        val provider = ProcessCameraProvider.awaitInstance(context)
        val selector =
            if (provider.hasCamera(
                    CameraSelector.DEFAULT_BACK_CAMERA,
                )
            ) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
        provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        try {
            awaitCancellation()
        } finally {
            provider.unbind(preview, imageCapture)
        }
    }
    surfaceRequest?.let { request -> CameraXViewfinder(surfaceRequest = request, modifier = modifier) }
}

private suspend fun ImageCapture.captureUprightJpeg(): ByteArray? =
    suspendCancellableCoroutine { continuation ->
        takePicture(
            Dispatchers.Default.asExecutor(),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    continuation.resume(image.use { it.toUprightJpeg() })
                }

                override fun onError(exception: ImageCaptureException) {
                    Logger.w(exception) { "Camera capture failed" }
                    continuation.resume(null)
                }
            },
        )
    }

private fun ImageProxy.toUprightJpeg(): ByteArray {
    val bitmap = toBitmap()
    val scale = minOf(1f, MAX_PHOTO_EDGE.toFloat() / maxOf(bitmap.width, bitmap.height))
    val matrix =
        Matrix().apply {
            postScale(scale, scale)
            postRotate(imageInfo.rotationDegrees.toFloat())
        }
    val upright = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return ByteArrayOutputStream().use { output ->
        upright.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        output.toByteArray()
    }
}

private const val CAMERA = Manifest.permission.CAMERA
private const val MAX_PHOTO_EDGE = 2048
private const val JPEG_QUALITY = 90
