package com.jcoronado.minimalbitcoinwidget.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jcoronado.minimalbitcoinwidget.AppDatabase
import com.jcoronado.minimalbitcoinwidget.DebugLog
import com.jcoronado.minimalbitcoinwidget.R
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import android.widget.Toast
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeveloperOptionsScreen(
    mockUiEnabled: Boolean,
    onMockUiToggle: (Boolean) -> Unit,
    persistedPrice: String,
    persistedPercentChange: String,
    persistedCurrency: String,
    onApplyChanges: (price: String, percentChange: String, currency: String) -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val view = LocalView.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val mockUiAppliedMessage = stringResource(R.string.debug_mock_ui_applied)

    var priceInput by remember(persistedPrice) { mutableStateOf(persistedPrice) }
    var percentChangeInput by remember(persistedPercentChange) { mutableStateOf(persistedPercentChange) }
    var currencyInput by remember(persistedCurrency) { mutableStateOf(persistedCurrency) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.developer_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }, colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ), scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                item {
                    val colors =
                        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 0, count = if (mockUiEnabled) 3 else 2
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_notes_24), "TODO"
                            )
                        },
                        content = {
                            Text(stringResource(R.string.widget_logs), fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text(stringResource(R.string.widget_logs_description))
                        },
                        trailingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_chevron_forward_24), "TODO"
                            )
                        },
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onNavigateToLogs()
                        },
                    )
                }
                item {
                    val colors =
                        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 1, count = if (mockUiEnabled) 3 else 2
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_bug_report_24), "TODO"
                            )
                        },
                        content = {
                            Text(stringResource(R.string.debug_mock_ui_title), fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text(stringResource(R.string.debug_mock_ui_desc))
                        },
                        trailingContent = {
                            Switch(
                                checked = mockUiEnabled, onCheckedChange = null
                            )
                        },
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onMockUiToggle(!mockUiEnabled)
                        },
                    )
                }
                if (mockUiEnabled) {
                    item {
                        val colors =
                            ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
                        CompositionLocalProvider(LocalRippleConfiguration provides null) {
                            SegmentedListItem(
                                colors = colors,
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = 2, count = 3
                                ),
                                content = {},
                                supportingContent = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = priceInput,
                                            onValueChange = { priceInput = it },
                                            label = { Text(stringResource(R.string.debug_price_label)) },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = percentChangeInput,
                                            onValueChange = { percentChangeInput = it },
                                            label = { Text(stringResource(R.string.debug_change_label)) },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = currencyInput,
                                            onValueChange = { currencyInput = it },
                                            label = { Text(stringResource(R.string.debug_currency_label)) },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Done
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                keyboardController?.hide()
                                                onApplyChanges(priceInput, percentChangeInput, currencyInput)
                                                Toast.makeText(context, mockUiAppliedMessage, Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text(stringResource(R.string.debug_apply_btn))
                                        }
                                    }
                                },
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

// TODO - move to a separate file
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetLogsScreen(onBack: () -> Unit) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // get the db
    val db = remember { AppDatabase.getInstance(context) }

    // observe the logs
    val logs by db.debugDao().getAllLogs().collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                Text(
                    stringResource(R.string.widget_logs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }, navigationIcon = {
                IconButton(onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onBack()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_arrow_back_24),
                        contentDescription = stringResource(R.string.back_icon_description)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ), actions = {
                TextButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        scope.launch { db.debugDao().clearAll() }
                    },
                    enabled = logs.isNotEmpty()
                ) {
                    Text(stringResource(R.string.clear))
                }
            }, scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs stored.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    itemsIndexed(logs) { index, log ->
                        if (index != logs.size - 1) {
                            LogItem(index, log, logs.size)
                        } else {
                            LogItem(index, log, logs.size)
                            Spacer(
                                Modifier.height(
                                    WindowInsets.systemBars.asPaddingValues()
                                        .calculateBottomPadding() + 0.dp
                                )
                            )
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LogItem(index: Int, log: DebugLog, length: Int) {
    val colors =
        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        SegmentedListItem(
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(
                index = index, count = length
            ),
            content = {
                Text(
                    text = log.timestamp, fontSize = 12.sp
                )
            },
            supportingContent = {
                Text(
                    text = log.message, fontSize = 16.sp
                )
            },
            onClick = {},
        )
    }
}
