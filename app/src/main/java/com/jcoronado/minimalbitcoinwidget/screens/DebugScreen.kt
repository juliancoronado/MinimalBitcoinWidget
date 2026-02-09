package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jcoronado.minimalbitcoinwidget.AppDatabase
import com.jcoronado.minimalbitcoinwidget.DebugLog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DebugScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // get the db
    val db = remember { AppDatabase.getInstance(context) }

    // observe the logs
    val logs by db.debugDao().getAllLogs().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Debug Logs")
                }, colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ), actions = {
                    TextButton(
                        onClick = { scope.launch { db.debugDao().clearAll() } },
                        enabled = logs.isNotEmpty()
                    ) {
                        Text("Clear")
                    }
                })
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
                itemsIndexed(logs) { index, log ->
                    if (index != logs.size - 1) {
                        LogItem(index, log, logs.size)
                    } else {
                        LogItem(index, log, logs.size)
                        Spacer(
                            Modifier.height(
                                WindowInsets.systemBars.asPaddingValues()
                                    .calculateBottomPadding() + 100.dp
                            )
                        )
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