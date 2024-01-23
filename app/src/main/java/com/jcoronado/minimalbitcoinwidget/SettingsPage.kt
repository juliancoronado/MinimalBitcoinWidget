@file:OptIn(ExperimentalMaterial3Api::class)

package com.jcoronado.minimalbitcoinwidget

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                            text = "Settings",
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
    Surface(modifier = Modifier.padding(top = 8.dp)) {
        Column(modifier = Modifier.padding(padding)) {
            MenuTitle(text = "Selected Currency".uppercase())
            MenuOption(text = "U.S. Dollar (\$)")
        }
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
fun MenuOption(text: String) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dialog is open") },
            text = { Text("Dialog options here") },
            confirmButton = {
                TextButton(onClick = { /* TODO */}) {
                    Text("Delete it".uppercase())
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
        Text(text = text, modifier = Modifier.padding(all = 16.dp))
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun SettingsPagePreview() {
//    SettingsPage()
//}