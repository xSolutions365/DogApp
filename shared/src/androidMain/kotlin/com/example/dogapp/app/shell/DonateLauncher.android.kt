package com.example.dogapp.app.shell

import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.dogapp.app.navigation.NavigationStyle
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.donate_google_pay_demo_notice
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberDonateLauncher(
    navigationStyle: NavigationStyle,
    snackbarHostState: SnackbarHostState,
    onOpenWallet: () -> Unit,
): (String) -> Unit {
    val context = LocalContext.current
    val demoNotice = stringResource(Res.string.donate_google_pay_demo_notice)
    return rememberMaterialDonateSheet(snackbarHostState, onOpenWallet) {
        Toast.makeText(context, demoNotice, Toast.LENGTH_LONG).show()
    }
}
