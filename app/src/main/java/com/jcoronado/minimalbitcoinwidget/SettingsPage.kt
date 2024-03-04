@file:OptIn(ExperimentalMaterial3Api::class)

package com.jcoronado.minimalbitcoinwidget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.ui.theme.BTCPriceWidgetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(priceViewModel: PriceViewModel, onBackButtonPressed: () -> Unit) {
    BTCPriceWidgetTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.settings),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackButtonPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            SettingsMenu(priceViewModel, innerPadding)
        }
    }
}

@Composable
fun SettingsMenu(priceViewModel: PriceViewModel, padding: PaddingValues) {
    Column(modifier = Modifier.padding(padding)) {
        CurrencySection(priceViewModel = priceViewModel)
        HorizontalDivider(thickness = Dp.Hairline, color = Color.Gray)
        Text(
            text = "${stringResource(id = R.string.version_title)} ${stringResource(id = R.string.version_summary)}",
            modifier = Modifier.padding(all = 16.dp)
        )
    }
}

@Composable
fun CurrencySection(priceViewModel: PriceViewModel) {
    Column {
        MenuTitle(text = "Currency Options".uppercase())
        MenuListTile(priceViewModel = priceViewModel)
    }
}

@Composable
fun MenuTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
    )
}

@Composable
fun MenuListTile(priceViewModel: PriceViewModel) {
    // TODO - work in progress - dialog radio button inconsistencies
    var showDialog by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("") }
    val subtitle = "Selected Currency"
    // val symbol by priceViewModel.symbol.collectAsState(initial = "")
    val currencyCode by priceViewModel.currencyCode.collectAsState(initial = "usd")

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Currency") },
            text = {
                DialogBody(
                    currentCurrency = currencyCode ?: "usd",
                    onCurrencySelected = {
                        selectedCurrency = it
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        priceViewModel.updateSelectedCurrency(selectedCurrency)
                        showDialog = false
                    }
                ) {
                    Text("Update".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel".uppercase())
                }
            },
        )
    }

    val textToDisplay : String = if (currencyCode.isNullOrBlank()) {
        "USD"
    } else {
        currencyCode!!
    }

    Surface(
        onClick = { showDialog = showDialog.not() },
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(textToDisplay) },
            supportingContent = {
                Text(text = subtitle)
            })
    }
}

@Composable
fun DialogBody(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    // Array <String>
    val radioValues = stringArrayResource(id = R.array.currency_values)
    val radioNames = stringArrayResource(id = R.array.currency_entries)
    val selectedOption =
        remember { mutableStateOf(currentCurrency) }

    Column {
        radioValues.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption.value),
                        onClick = {
                            if (option != selectedOption.value) {
                                selectedOption.value = option
                                onCurrencySelected(option)
                            }
                        }
                    )
                    .padding(horizontal = 8.dp)
            ) {
                RadioButton(
                    selected = (option == selectedOption.value),
                    modifier = Modifier.padding(horizontal = Dp(value = 8F)),
                    onClick = {
                        if (option != selectedOption.value) {
                            selectedOption.value = option
                            onCurrencySelected(option)
                        }
                    }
                )
                Text(
                    text = radioNames[radioValues.indexOf(option)],
                    modifier = Modifier.align(alignment = Alignment.CenterVertically)
                )
            }
        }
    }
}

// apiLevel = 33 - work around to get preview to render
@Preview(apiLevel = 33, showSystemUi = true)
@Composable
fun SettingsPagePreview() {
    val dataStoreManager = DataStoreManager(LocalContext.current)
    val priceViewModel = PriceViewModel(dataStoreManager = dataStoreManager)
    SettingsPage(priceViewModel = priceViewModel, onBackButtonPressed = {})
}