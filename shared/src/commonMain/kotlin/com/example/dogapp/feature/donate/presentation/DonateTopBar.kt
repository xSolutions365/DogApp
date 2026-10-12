package com.example.dogapp.feature.donate.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.dogapp.core.presentation.components.platformBackIcon
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.detail_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DonateTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(painterResource(platformBackIcon), contentDescription = stringResource(Res.string.detail_back))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.weight(1f).padding(end = 16.dp).semantics { heading() },
        )
    }
}
