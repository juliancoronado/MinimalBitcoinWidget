package com.jcoronado.minimalbitcoinwidget.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.viewmodels.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit = {},
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit = {},
    dynamicColors: Boolean,
    onDynamicColorsSelected: (Boolean) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val showCurrencyDialog = remember { mutableStateOf(false) }
    var newSelection by remember { mutableStateOf(selectedCurrency) }
    val currencyDescriptions = stringArrayResource(R.array.currency_entries)
    val currencyValues = stringArrayResource(R.array.currency_values)
    var refreshInterval by remember { mutableIntStateOf(0) }
    var changePercentage by remember { mutableIntStateOf(0) }

    if (showCurrencyDialog.value) {
        AlertDialog(
            modifier = Modifier.heightIn(min = 200.dp, max = 400.dp),
            onDismissRequest = { showCurrencyDialog.value = false },
            // TODO - replace with string resource value
            title = { Text("Update Currency") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        currencyValues.forEachIndexed { index, currency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (currency.equals(
                                            newSelection, ignoreCase = true
                                        )), onClick = {
                                            newSelection = currency
                                        })
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (currency.equals(newSelection, ignoreCase = true)),
                                    onClick = null // row handles click
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${currencyDescriptions[index]} - ${currency.uppercase()}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 0.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCurrencyDialog.value = false
                    onCurrencySelected(newSelection)
                }) {
                    // TODO - replace with string resource value
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog.value = false }) {
                    // TODO - replace with string resource value
                    Text("Cancel")
                }
            })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                }, colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val colors =
                ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                // TODO - replace hard-coded strings with string resources in SectionHeaders
                SectionHeader("Data", true)
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_currency_exchange_24), "TBD"
                        )
                    },
                    content = {
                        Text(currencyDescriptions[currencyValues.indexOf(selectedCurrency)])
                    },
                    supportingContent = {
                        // TODO - replace with string resource value
                        Text("Local Currency", modifier = Modifier.padding(top = 4.dp))
                    },
                    onClick = {
                        // show options dialog
                        newSelection = selectedCurrency // reset to current value for UI
                        showCurrencyDialog.value = true
                    },
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_price_change_24), null
                        )
                    },
                    content = {
                        // TODO - implement change percentage setting (requires API change)
                        Text("Change Percentage (coming soon)")
                    },
                    supportingContent = {
                        ButtonGroup(
                            overflowIndicator = { },
                            modifier = Modifier.padding(top = 8.dp),
                            expandedRatio = 0.02F,
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalAlignment = Alignment.Top,
                            content = {
                                this.toggleableItem(
                                    checked = changePercentage == 0,
                                    // TODO - replace with string resource value
                                    label = "24 Hours",
                                    weight = if (changePercentage == 0) 1F else 0.9F,
                                    onCheckedChange = {
                                        changePercentage = 0
                                    },
                                )
                                this.toggleableItem(
                                    checked = changePercentage == 1,
                                    // TODO - replace with string resource value
                                    label = "7 Days",
                                    weight = if (changePercentage == 1) 1F else 0.9F,
                                    onCheckedChange = {
                                        changePercentage = 1
                                    },
                                )
                                this.toggleableItem(
                                    checked = changePercentage == 2,
                                    // TODO - replace with string resource value
                                    label = "30 Days",
                                    weight = if (changePercentage == 2) 1F else 0.9F,
                                    onCheckedChange = {
                                        changePercentage = 2
                                    },
                                )
                            },
                        )
                    },
                    onClick = {},
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 2, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_timer_24), null
                        )
                    },
                    content = {
                        // TODO - implement refresh interval setting
                        Text("Refresh Interval (coming soon)")
                    },
                    supportingContent = {
                        ButtonGroup(
                            overflowIndicator = { },
                            modifier = Modifier.padding(top = 8.dp),
                            expandedRatio = 0.02F,
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalAlignment = Alignment.Top,
                            content = {
                                this.toggleableItem(
                                    checked = refreshInterval == 0,
                                    // TODO - replace with string resource value
                                    label = "30 Mins",
                                    weight = if (refreshInterval == 0) 1F else 0.9F,
                                    onCheckedChange = {
                                        refreshInterval = 0
                                    },
                                )
                                this.toggleableItem(
                                    checked = refreshInterval == 1,
                                    // TODO - replace with string resource value
                                    label = "1 Hour",
                                    weight = if (refreshInterval == 1) 1F else 0.9F,
                                    onCheckedChange = {
                                        refreshInterval = 1
                                    },
                                )
                                this.toggleableItem(
                                    checked = refreshInterval == 2,
                                    // TODO - replace with string resource value
                                    label = "4 Hours",
                                    weight = if (refreshInterval == 2) 1F else 0.9F,
                                    onCheckedChange = {
                                        refreshInterval = 2
                                    },
                                )
                            },
                        )
                    },
                    onClick = {},
                )
                SectionHeader("Appearance")
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 2
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_brightness_6_24), null
                        )
                    },
                    content = {
                        // TODO - replace with string resource value
                        Text("App Theme")
                    },
                    supportingContent = {
                        ButtonGroup(
                            overflowIndicator = { },
                            modifier = Modifier.padding(top = 8.dp),
                            expandedRatio = 0.02F,
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            verticalAlignment = Alignment.Top,
                            content = {
                                this.toggleableItem(
                                    checked = currentTheme == AppTheme.SYSTEM,
                                    // TODO - replace with string resource value
                                    label = "System",
                                    weight = if (currentTheme == AppTheme.SYSTEM) 1F else 0.9F,
                                    onCheckedChange = {
                                        onThemeSelected(AppTheme.SYSTEM)
                                    },
                                )
                                this.toggleableItem(
                                    checked = currentTheme == AppTheme.LIGHT,
                                    // TODO - replace with string resource value
                                    label = "Light",
                                    weight = if (currentTheme == AppTheme.LIGHT) 1F else 0.9F,
                                    onCheckedChange = {
                                        onThemeSelected(AppTheme.LIGHT)
                                    },
                                )
                                this.toggleableItem(
                                    checked = currentTheme == AppTheme.DARK,
                                    // TODO - replace with string resource value
                                    label = "Dark",
                                    weight = if (currentTheme == AppTheme.DARK) 1F else 0.9F,
                                    onCheckedChange = {
                                        onThemeSelected(AppTheme.DARK)
                                    },
                                )
                            },
                        )
                    },
                    onClick = {},
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1, count = 2
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_palette_24), null
                        )
                    },
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    content = {
                        // TODO - replace with string resource value
                        Text("Dynamic Colors")
                    },
                    supportingContent = {
                        Text(
                            // TODO - replace with string resource value
                            "Follow colors from your device theme",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                            onCheckedChange = null,
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        )
                    },
                    onClick = {
                        onDynamicColorsSelected(!dynamicColors)
                    },
                )
                SectionHeader("App Info")
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_info_24), "TBD"
                        )
                    },
                    content = {
                        Text(stringResource(R.string.version_only_main))
                    },
                    supportingContent = {
                        // TODO - replace with string resource value
                        Text("Version")
                    },
                    onClick = {},
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_build_24), "TBD"
                        )
                    },
                    content = {
                        Text(stringResource(R.string.version_only_build))
                    },
                    supportingContent = {
                        // TODO - replace with string resource value
                        Text("Build Number")
                    },
                    onClick = {},
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 2, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_alternate_email_24), null
                        )
                    },
                    content = {
                        Text("jcoronado.dev")
                    },
                    supportingContent = {
                        // TODO - replace with string resource value
                        Text("Contact Developer")
                    },
                    onClick = {
                        // TODO - implement this to open an email client / browser
                    },
                )
                // to prevent list items from hiding under the floating horizontal bar
                Spacer(
                    Modifier.height(
                        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 100.dp
                    )
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, top: Boolean = false) {
    Text(
        title,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .padding(top = if (top) 4.dp else 16.dp, bottom = 8.dp)
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SettingsPreview() {
    SettingsScreen(
        selectedCurrency = "usd", currentTheme = AppTheme.LIGHT, dynamicColors = true
    )
}
