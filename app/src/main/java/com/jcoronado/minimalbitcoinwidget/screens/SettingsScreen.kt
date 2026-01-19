package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    selectedCurrency: String
) {
    val scrollState = rememberScrollState()

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
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var selected by remember { mutableIntStateOf(0) }
            val colors =
                ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                Text(
                    "Data", modifier = Modifier.padding(vertical = 8.dp)
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_currency_exchange_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text(selectedCurrency.uppercase())
                    },
                    supportingContent = {
                        Text("Currency")
                    },
                    onClick = {
                        // display options dialog
                    },
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_date_range_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("Change Percentage")
                    },
                    supportingContent = {
                        ButtonGroup(
                            modifier = Modifier.fillMaxWidth(),
                            overflowIndicator = { menuState ->
                                ButtonGroupDefaults.OverflowIndicator(
                                    menuState = menuState
                                )
                            },
                            expandedRatio = ButtonGroupDefaults.ExpandedRatio,
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top,
                            content = {
                                this.toggleableItem(
                                    checked = true,
                                    label = "24 Hours",
                                    onCheckedChange = {},
                                )
                                this.toggleableItem(
                                    checked = false,
                                    label = "7 Days",
                                    onCheckedChange = {},
                                )
                                this.toggleableItem(
                                    checked = false,
                                    label = "30 Days",
                                    onCheckedChange = {},
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
                            painterResource(R.drawable.rounded_timer_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("Refresh Interval")
                    },
                    supportingContent = {
                        ButtonGroup(
                            overflowIndicator = { menuState ->
                                ButtonGroupDefaults.OverflowIndicator(
                                    menuState = menuState
                                )
                            },
                            expandedRatio = ButtonGroupDefaults.ExpandedRatio,
                            horizontalArrangement = ButtonGroupDefaults.HorizontalArrangement,
                            verticalAlignment = Alignment.Top,
                            content = {
                                this.toggleableItem(
                                    checked = true,
                                    label = "30 Mins",
                                    onCheckedChange = {},
                                )
                                this.toggleableItem(
                                    checked = false,
                                    label = "1 Hour",
                                    onCheckedChange = {},
                                )
                                this.toggleableItem(
                                    checked = false,
                                    label = "4 Hours",
                                    onCheckedChange = {},
                                )
                            },
                        )
                    },
                    onClick = {},
                )
                Text(
                    "Appearance", modifier = Modifier.padding(vertical = 8.dp)
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 2
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_brightness_6_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("App Theme")
                    },
                    supportingContent = {
                        ButtonGroup(
                            modifier = Modifier.fillMaxWidth(),
                            overflowIndicator = {},
                            expandedRatio = 0.02F,
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top,
                            content = {
                                this.toggleableItem(
                                    checked = selected == 0,
                                    label = "System",
                                    onCheckedChange = {
                                        selected = 0
                                    },
                                )
                                this.toggleableItem(
                                    checked = selected == 1,
                                    label = "Light",
                                    onCheckedChange = {
                                        selected = 1
                                    },
                                )
                                this.toggleableItem(
                                    checked = selected == 2,
                                    label = "Dark",
                                    onCheckedChange = {
                                        selected = 2
                                    },
                                )
                            },
                        )
                    },
                    onClick = {
                        // display options dialog
                    },
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1, count = 2
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_palette_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("Dynamic Colors")
                    },
                    trailingContent = {
                        Switch(checked = true, onCheckedChange = {})
                    },
                    onClick = {},
                )
                Text(
                    "App Info", modifier = Modifier.padding(vertical = 8.dp)
                )
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_info_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("1.0.0")
                    },
                    supportingContent = {
                        Text("App Version")
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
                            painterResource(R.drawable.rounded_build_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("12")
                    },
                    supportingContent = {
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
                            painterResource(R.drawable.rounded_alternate_email_24),
                            "TBD"
                        )
                    },
                    content = {
                        Text("jcoronado.dev")
                    },
                    supportingContent = {
                        Text("Contact Developer")
                    },
                    onClick = {},
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SettingsPreview() {
    SettingsScreen(
        selectedCurrency = "USD")
}
