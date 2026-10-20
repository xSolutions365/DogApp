package com.example.dogapp.feature.home.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.home_choose_photo
import dogapp.shared.generated.resources.home_identify_action
import dogapp.shared.generated.resources.home_take_photo
import dogapp.shared.generated.resources.ic_photo_camera
import dogapp.shared.generated.resources.ic_photo_library
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun IdentifyMenuButton(
    onTakePhotoClick: () -> Unit,
    onChoosePhotoClick: () -> Unit,
    modifier: Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(stringResource(Res.string.home_identify_action), style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.home_take_photo)) },
                leadingIcon = { Icon(painterResource(Res.drawable.ic_photo_camera), contentDescription = null) },
                onClick = {
                    expanded = false
                    onTakePhotoClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.home_choose_photo)) },
                leadingIcon = { Icon(painterResource(Res.drawable.ic_photo_library), contentDescription = null) },
                onClick = {
                    expanded = false
                    onChoosePhotoClick()
                },
            )
        }
    }
}
