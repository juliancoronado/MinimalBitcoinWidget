package com.jcoronado.minimalbitcoinwidget.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jcoronado.minimalbitcoinwidget.AppDatabase
import com.jcoronado.minimalbitcoinwidget.DebugLog
import com.jcoronado.minimalbitcoinwidget.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeveloperOptionsScreen(onNavigateToLogs: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.developer_options_title))
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
                            index = 0, count = 2
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_notes_24),
                                "TODO"
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
                                painterResource(R.drawable.rounded_chevron_forward_24),
                                "TODO"
                            )
                        },
                        onClick = onNavigateToLogs,
                    )
                }
                item {
                    val colors =
                        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        SegmentedListItem(
                            colors = colors,
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 1, count = 2
                            ),
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.rounded_bug_report_24),
                                    "TODO"
                                )
                            },
                            content = {
                                Text("TODO", fontWeight = FontWeight.Bold)
                            },
                            supportingContent = {
                                Text("TODO")
                            },
                            onClick = {},
                        )
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
                    Text(stringResource(R.string.widget_logs))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_arrow_back_24),
                            contentDescription = stringResource(R.string.back_icon_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ), actions = {
                    TextButton(
                        onClick = { scope.launch { db.debugDao().clearAll() } },
                        enabled = logs.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }, scrollBehavior = scrollBehavior)
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
