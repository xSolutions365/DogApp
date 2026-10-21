package com.example.dogapp.feature.identify.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.camera_access_off
import dogapp.shared.generated.resources.camera_access_off_body
import dogapp.shared.generated.resources.camera_allow
import dogapp.shared.generated.resources.camera_close
import dogapp.shared.generated.resources.camera_open_settings
import dogapp.shared.generated.resources.camera_shutter
import dogapp.shared.generated.resources.camera_unavailable
import dogapp.shared.generated.resources.ic_close
import dogapp.shared.generated.resources.identify_choose_photo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CameraScreen(
    access: CameraAccess?,
    isStarting: Boolean,
    isCapturing: Boolean,
    onShutterClick: () -> Unit,
    onAllowAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onChoosePhoto: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewfinder: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when (access) {
            null -> {
                Unit
            }

            CameraAccess.Granted -> {
                viewfinder()
                Shutter(
                    isStarting = isStarting,
                    isCapturing = isCapturing,
                    onClick = onShutterClick,
                    modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(bottom = 32.dp),
                )
            }

            CameraAccess.Denied, CameraAccess.PermanentlyDenied, CameraAccess.Unavailable -> {
                NoCamera(
                    access = access,
                    onAllowAccess = onAllowAccess,
                    onOpenSettings = onOpenSettings,
                    onChoosePhoto = onChoosePhoto,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(8.dp)) {
            Icon(painterResource(Res.drawable.ic_close), contentDescription = stringResource(Res.string.camera_close), tint = Color.White)
        }
    }
}

@Composable
private fun Shutter(
    isStarting: Boolean,
    isCapturing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(Res.string.camera_shutter)
    FilledIconButton(
        onClick = onClick,
        enabled = !isStarting && !isCapturing,
        shape = CircleShape,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = SHUTTER_DISABLED_ALPHA),
            ),
        modifier =
            modifier
                .size(76.dp)
                .border(4.dp, Color.White.copy(alpha = SHUTTER_RING_ALPHA), CircleShape)
                .semantics { contentDescription = label },
    ) {
        if (isCapturing) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun NoCamera(
    access: CameraAccess,
    onAllowAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onChoosePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (access != CameraAccess.Unavailable) {
            Text(
                text = stringResource(Res.string.camera_access_off),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
        }
        Text(
            text =
                stringResource(
                    if (access ==
                        CameraAccess.Unavailable
                    ) {
                        Res.string.camera_unavailable
                    } else {
                        Res.string.camera_access_off_body
                    },
                ),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        when (access) {
            CameraAccess.Denied -> {
                Button(onClick = onAllowAccess) { Text(stringResource(Res.string.camera_allow)) }
            }

            CameraAccess.PermanentlyDenied -> {
                Button(onClick = onOpenSettings) { Text(stringResource(Res.string.camera_open_settings)) }
            }

            CameraAccess.Granted, CameraAccess.Unavailable -> {
                Unit
            }
        }
        OutlinedButton(onClick = onChoosePhoto) { Text(stringResource(Res.string.identify_choose_photo), color = Color.White) }
    }
}

private const val SHUTTER_DISABLED_ALPHA = 0.6f
private const val SHUTTER_RING_ALPHA = 0.5f
