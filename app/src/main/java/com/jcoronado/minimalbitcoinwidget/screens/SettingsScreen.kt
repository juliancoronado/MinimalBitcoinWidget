package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.Screen
import com.jcoronado.minimalbitcoinwidget.classes.SettingsNavItem

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Data,
        R.drawable.rounded_currency_exchange_24,
        R.string.settings_data,
        "Currency, percentage and interval"
    ), SettingsNavItem(
        Screen.Settings.Appearance,
        R.drawable.rounded_palette_24,
        R.string.settings_appearance,
        "Colors and app theme"
    ), SettingsNavItem(
        Screen.Settings.About,
        R.drawable.rounded_info_24,
        R.string.settings_about,
        "Version, build number and contact"
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigate: (Screen.Settings) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        ) {
            val colors =
                ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                settingsScreens.forEachIndexed { index, item ->
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index, count = settingsScreens.size
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(item.icon), null
                            )
                        },
                        supportingContent = { Text(item.subtitle) },
                        content = { Text(stringResource(item.label)) },
                        onClick = {
                            onNavigate(item.route)
                        },
                    )
                }
            }
        }
    }
}
