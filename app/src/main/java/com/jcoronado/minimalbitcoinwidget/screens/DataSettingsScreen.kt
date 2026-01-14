package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DataSettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_arrow_back_24),
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            val colors =
                ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            SegmentedListItem(
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    index = 0, count = 3
                ),
                content = { Text("USD") },
                supportingContent = { Text("Selected Currency") },
                onClick = {},
            )
            SegmentedListItem(
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    index = 1, count = 3
                ),
                content = { Text("24 hours") },
                supportingContent = { Text("Change Percentage") },
                onClick = {},
            )
            SegmentedListItem(
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(
                    index = 2, count = 3
                ),
                content = { Text("30 minutes") },
                supportingContent = { Text("Refresh Interval") },
                onClick = {
                    // onNavigate(item.route)
                },
            )

        }

    }
}