package com.example.dogapp.feature.profile.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.domain.security.IntegrityCheck
import com.example.dogapp.core.domain.security.IntegrityCheckKind
import com.example.dogapp.core.presentation.components.BackTopBar
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.device_check_app_signature
import dogapp.shared.generated.resources.device_check_app_signature_ok
import dogapp.shared.generated.resources.device_check_debugger
import dogapp.shared.generated.resources.device_check_debugger_ok
import dogapp.shared.generated.resources.device_check_emulator
import dogapp.shared.generated.resources.device_check_jailbreak_files
import dogapp.shared.generated.resources.device_check_root_or_magisk
import dogapp.shared.generated.resources.device_check_sandbox_write
import dogapp.shared.generated.resources.device_check_sandbox_write_ok
import dogapp.shared.generated.resources.device_check_simulator
import dogapp.shared.generated.resources.device_check_test_keys_build
import dogapp.shared.generated.resources.device_checks_intro
import dogapp.shared.generated.resources.device_checks_not_detected
import dogapp.shared.generated.resources.device_checks_not_found
import dogapp.shared.generated.resources.device_checks_warning
import dogapp.shared.generated.resources.profile_device_checks
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DeviceChecksRoot(
    onBack: () -> Unit,
    viewModel: DeviceChecksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DeviceChecksScreen(state = state, onBack = onBack)
}

@Composable
fun DeviceChecksScreen(
    state: DeviceChecksUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "title") { BackTopBar(title = stringResource(Res.string.profile_device_checks), onBack = onBack) }
        item(key = "intro") {
            Text(
                text = stringResource(Res.string.device_checks_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        items(state.checks.orEmpty(), key = { "check-${it.kind}" }) { check ->
            DeviceCheckRow(check = check)
        }
    }
}

@Composable
private fun DeviceCheckRow(
    check: IntegrityCheck,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(check.kind.label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = stringResource(if (check.isFlagged) Res.string.device_checks_warning else check.kind.passedText),
            style = MaterialTheme.typography.bodyLarge,
            color = if (check.isFlagged) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

private val IntegrityCheckKind.label: StringResource
    get() =
        when (this) {
            IntegrityCheckKind.RootOrMagisk -> Res.string.device_check_root_or_magisk
            IntegrityCheckKind.TestKeysBuild -> Res.string.device_check_test_keys_build
            IntegrityCheckKind.AppSignature -> Res.string.device_check_app_signature
            IntegrityCheckKind.Emulator -> Res.string.device_check_emulator
            IntegrityCheckKind.JailbreakFiles -> Res.string.device_check_jailbreak_files
            IntegrityCheckKind.SandboxWrite -> Res.string.device_check_sandbox_write
            IntegrityCheckKind.Simulator -> Res.string.device_check_simulator
            IntegrityCheckKind.Debugger -> Res.string.device_check_debugger
        }

private val IntegrityCheckKind.passedText: StringResource
    get() =
        when (this) {
            IntegrityCheckKind.RootOrMagisk,
            IntegrityCheckKind.TestKeysBuild,
            IntegrityCheckKind.JailbreakFiles,
            -> Res.string.device_checks_not_found

            IntegrityCheckKind.Emulator, IntegrityCheckKind.Simulator -> Res.string.device_checks_not_detected

            IntegrityCheckKind.AppSignature -> Res.string.device_check_app_signature_ok

            IntegrityCheckKind.SandboxWrite -> Res.string.device_check_sandbox_write_ok

            IntegrityCheckKind.Debugger -> Res.string.device_check_debugger_ok
        }
