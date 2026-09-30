package com.example.dogapp.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dogapp.navigation.TopLevelDestination
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val TabBarHeight = 49.dp
private val TabIconSize = 26.dp
private val TabIconLabelSpacing = 2.dp
private val TabLabelFontSize = 10.sp
private val TabLabelLetterSpacing = 0.1.sp
private val LightBarBackground = Color(0xFFF7F7F7)
private val DarkBarBackground = Color(0xFF161616)
private val LightSeparator = Color(0x4A3C3C43)
private val DarkSeparator = Color(0x99545458)
private val UnselectedTint = Color(0xFF8E8E93)

@Composable
fun CupertinoTabBar(
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (darkTheme) DarkBarBackground else LightBarBackground),
    ) {
        HorizontalDivider(thickness = Dp.Hairline, color = if (darkTheme) DarkSeparator else LightSeparator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(TabBarHeight)
                .selectableGroup(),
        ) {
            TopLevelDestination.entries.forEach { destination ->
                CupertinoTabItem(
                    destination = destination,
                    selected = destination == selectedDestination,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CupertinoTabItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else UnselectedTint
    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = null,
                indication = null,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(if (selected) destination.selectedIcon else destination.unselectedIcon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(TabIconSize),
        )
        Spacer(Modifier.height(TabIconLabelSpacing))
        Text(
            text = stringResource(destination.label),
            color = tint,
            fontSize = TabLabelFontSize,
            fontWeight = FontWeight.Medium,
            letterSpacing = TabLabelLetterSpacing,
        )
    }
}
