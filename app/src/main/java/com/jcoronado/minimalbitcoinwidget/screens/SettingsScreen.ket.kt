package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackButtonClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                }, colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ), navigationIcon = {
                    IconButton(onClick = onBackButtonClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = "Back Arrow Icon",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { SettingsHeader("Display") }

                item {
                    SettingsItem(
                        title = "USD", subtitle = "Selected Currency", onClick = {
                            // open dropdown / dialog
                        })
                    SettingsItem(
                        title = "30 minutes", subtitle = "Refresh Interval", onClick = {
                            // open dropdown / dialog
                        })
                }

                item {
                    HorizontalDivider(
                        thickness = DividerDefaults.Thickness / 2
                    )
                }

                item { SettingsHeader("App Info") }

                item {
                    SettingsItem(
                        title = stringResource(R.string.version_summary),
                        subtitle = stringResource(R.string.version_title),
                    )
                    SettingsItem(
                        title = stringResource(R.string.coingecko_summary),
                        subtitle = stringResource(R.string.coingecko_title),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String, subtitle: String? = null, onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
    ListItem(
        modifier = modifier,
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
    )
}

@Composable
fun SettingsHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onBackButtonClick = {})
}