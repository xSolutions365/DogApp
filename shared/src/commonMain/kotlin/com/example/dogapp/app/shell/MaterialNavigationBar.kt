package com.example.dogapp.app.shell

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dogapp.app.navigation.TopLevelDestination
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MaterialNavigationBar(
    selectedDestination: TopLevelDestination,
    onDestinationSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = destination == selectedDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelect(destination) },
                icon = {
                    Icon(
                        painter = painterResource(if (selected) destination.selectedIcon else destination.unselectedIcon),
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}
