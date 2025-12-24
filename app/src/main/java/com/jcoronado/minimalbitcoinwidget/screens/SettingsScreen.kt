package com.jcoronado.minimalbitcoinwidget.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
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
                    var expanded by remember { mutableStateOf(false) }
                    val currencyEntries = stringArrayResource(id = R.array.currency_entries)
                    val currencyValues = stringArrayResource(id = R.array.currency_values)
                    
                    val selectedIndex = currencyValues.indexOf(selectedCurrency).coerceAtLeast(0)
                    val currentEntry = currencyEntries[selectedIndex]

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        SettingsItem(
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            title = currentEntry,
                            subtitle = stringResource(R.string.currency_title),
                            onClick = { expanded = true }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            currencyEntries.forEachIndexed { index, entry ->
                                DropdownMenuItem(
                                    text = { Text(entry) },
                                    onClick = {
                                        onCurrencySelected(currencyValues[index])
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    SettingsItem(
                        title = "24 hours",
                        subtitle = "Change Percentage",
                        onClick = {
                            // TODO - implement change percentage setting
                            Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show()
                        }
                    )

                    SettingsItem(
                        title = "30 minutes", 
                        subtitle = "Refresh Interval", 
                        onClick = {
                            // TODO - implement refresh interval setting
                            Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        thickness = DividerDefaults.Thickness / 2
                    )
                }

                item { SettingsHeader(stringResource(R.string.pref_info)) }

                item {
                    SettingsItem(
                        title = stringResource(R.string.coingecko_summary),
                        subtitle = stringResource(R.string.coingecko_title),
                    )
                    SettingsItem(
                        title = stringResource(R.string.version_summary),
                        subtitle = stringResource(R.string.version_title),
                    )
                    SettingsItem(
                        title = stringResource(R.string.dev_summary),
                        subtitle = stringResource(R.string.pref_developer),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val combinedModifier = modifier.then(
        if (onClick != null) Modifier.clickable { onClick() } else Modifier
    )
    ListItem(
        modifier = combinedModifier,
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
    SettingsScreen(
        selectedCurrency = AppConstants.CURRENCY_DEFAULT,
        onCurrencySelected = {},
        onBackButtonClick = {}
    )
}
