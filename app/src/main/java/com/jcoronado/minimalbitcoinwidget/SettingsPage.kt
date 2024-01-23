@file:OptIn(ExperimentalMaterial3Api::class)

package com.jcoronado.minimalbitcoinwidget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.ui.theme.BTCPriceWidgetTheme

@Composable
fun SettingsPage(onBackButtonPressed: () -> Unit) {
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
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            SettingsMenu(innerPadding)
        }
    }
}

@Composable
fun SettingsMenu(padding: PaddingValues) {
    Column(modifier = Modifier.padding(padding)) {
        CurrencySection()
        Divider(color = Color.Gray, thickness = Dp.Hairline)
        Text(
            text = "${stringResource(id = R.string.version_title)} ${stringResource(id = R.string.version_summary)}",
            modifier = Modifier.padding(all = 16.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CurrencySection() {
    Column {
        MenuTitle(text = "Currency Options".uppercase())
        MenuListTile(title = "U.S. Dollar (\$)", subtitle = "Selected Currency")
    }
}

@Composable
fun MenuTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
fun MenuListTile(title: String, subtitle: String) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Currency") },
            text = { DialogBody() },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
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

    Surface(
        onClick = { showDialog = showDialog.not() },
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(headlineContent = { Text(text = title) }, supportingContent = {
            Text(text = subtitle)
        })
    }
}

@Composable
fun DialogBody() {
    val radioOptions = stringArrayResource(id = R.array.currency_entries)
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    Column {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    // using modifier to add max
                    // width to our radio button.
                    .fillMaxWidth()
                    // below method is use to add
                    // selectable to our radio button.
                    .selectable(
                        // this method is called when
                        // radio button is selected.
                        selected = (text == selectedOption),
                        // below method is called on
                        // clicking of radio button.
                        onClick = { onOptionSelected(text) })
                    // below line is use to add
                    // padding to radio button.
                    .padding(horizontal = 8.dp)
            ) {

                // below line is use to
                // generate radio button
                RadioButton(
                    // inside this method we are
                    // adding selected with a option.
                    selected = (text == selectedOption),
                    modifier = Modifier.padding(horizontal = Dp(value = 8F)),
                    onClick = {
                        // inside on click method we are setting a
                        // selected option of our radio buttons.
                        onOptionSelected(text)
                    }
                )
                // below line is use to add
                // text to our radio buttons.
                Text(
                    text = text,
                    modifier = Modifier.align(alignment = Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsPagePreview() {
    SettingsPage(onBackButtonPressed = {})
}